package com.example.test.service

import com.example.test.entity.Keyword
import com.example.test.entity.RelatedKeyword
import com.example.test.entity.Search
import com.example.test.repository.KeywordRepository
import com.example.test.repository.RelatedKeywordRepository
import com.example.test.repository.SearchRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Instant
import kotlin.math.ceil

@Service
class SearchService @Autowired constructor(
        private val searchRepository: SearchRepository
        , private val keywordRepository: KeywordRepository
        , private val relatedKeywordRepository: RelatedKeywordRepository
        , @Qualifier("urlKakaoBlogApi") private val urlKakaoBlogApi:String
        , @Qualifier("keyKakaoApi") private val keyKakaoApi:String
        , @Qualifier("urlNaverBlogApi") private val urlNaverBlogApi:String
        , @Qualifier("idNaverApi") private val idNaverApi:String
        , @Qualifier("pwNaverApi") private val pwNaverApi:String
        , @Qualifier("urlOpenAiApi") private val urlOpenAiApi:String
        , @Qualifier("keyOpenAiApi") private val keyOpenAiApi:String
) {

    private val restTemplate = RestTemplate()



    //========================================================================
    // 1. 검색어 검색 횟수 증가 함수.
    // 3. Param : sKeyword
    // 4. 설명 : 인기 검색어를 위한 검색어 검색횟수 증가 함수.
    // 5. 작성 : bk.choi(2023-03-22)
    // 6. 수정 :
    //========================================================================
    @Transactional
    fun increaseKeywordCount(sKeyword:String){
        val keyword:Keyword = keywordRepository.findById(sKeyword)
                .orElse(Keyword(sKeyword, 0))
        keyword.keywordCnt = keyword.keywordCnt + 1
        keywordRepository.save(keyword)
    }


    //========================================================================
    // 1. 블로그 검색 내용 가져오는 함수.
    // 3. Param : sApiType, sQuery, sSortType, sPageNum
    // 4. 설명 : 5분 이내 검색한 결과가 DB에 임시저장 되어있으면 가져오고
    //          없으면 API를 호출한다.
    // 5. 작성 : bk.choi(2023-03-22)
    // 6. 수정 :
    //========================================================================
    fun askBlog(sApiType:String, sQuery:String, sSortType:String, sPageNum:String):HashMap<String, Any>?{
        val mapper = jacksonObjectMapper()

        //먼저 DB에서 최근 검색 결과가 있는지 확인
        val sParam = mapper.writeValueAsString(arrayOf(sApiType, sQuery, sSortType, sPageNum))
        val search = getSearch(sParam)

        val nTimestamp = Instant.now().toEpochMilli()

        var sJson:String? = null

        if(nTimestamp < search.timestamp+ 5*60*1000){
            //검색결과가 일정 시간 이내면 사용
            sJson = search.result
        }else if("kakao".equals(sApiType)){
            //카카오 api
            sJson = askKakaoBlog(sQuery, sSortType, sPageNum)
        }else if("naver".equals(sApiType)) {
            //네이버 api
            sJson = askNaverBlog(sQuery, sSortType, sPageNum)
        }else if("기타 추가 검색소스".equals(sApiType)){
            /*
               기능 요구사항
                1. 블로그 검색
                 - ...
               > - 추후 카카오 API 이외에 새로운 검색 소스가 추가될 수 있음을 고려해야 합니다.
            */
        }


        //에러 등으로 응답에 문제가 있으면 null
        if(sJson===null) return null

        //검색결과 저장(현재시간으로)
        searchRepository.save(Search(sParam, nTimestamp, sJson))

        return mapper.readValue(sJson,object : TypeReference<HashMap<String, Any>>() {})
    }


    //========================================================================
    // 1. 임시 저장 결과 가져오기 함수.
    // 3. Param : sParam
    // 4. 설명 : DB에서 임시저장된 검색결과를 가져오는 함수.
    // 5. 작성 : bk.choi(2023-03-22)
    // 6. 수정 :
    //========================================================================
    @Transactional
    private fun getSearch(sParam:String): Search{
        return searchRepository.findById(sParam)
                .orElse(Search(sParam, -1L, "")).also { searchRepository.save(it) }
    }


    //========================================================================
    // 1. 카카오 API 호출 함수.
    // 3. Param : sQuery, sSortType, sPageNum
    // 4. 설명 : 카카오 블로그 API 호출 함수.
    // 5. 작성 : bk.choi(2023-03-22)
    // 6. 수정 :
    //========================================================================
    private fun askKakaoBlog(sQuery:String, sSortType:String, sPageNum:String):String?{
        var sResult:String? = null
        try{
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.set("Authorization", "KakaoAK $keyKakaoApi")
            val uri = "$urlKakaoBlogApi?query=$sQuery&sort=$sSortType&page=$sPageNum"
            val entity = HttpEntity(null, headers)
            val response = restTemplate.exchange(uri, HttpMethod.GET, entity, String::class.java)
            sResult = response.body
        }catch (e:Exception){ println(e.toString()) }
        return sResult
    }


    //========================================================================
    // 1. 네이버 API 호출 함수.
    // 3. Param : sQuery, sSortType, sPageNum
    // 4. 설명 : 카카오 블로그 API 호출 함수.
    // 5. 작성 : bk.choi(2023-03-22)
    // 6. 수정 :
    //========================================================================
    private fun askNaverBlog(sQuery:String, sSortType:String, sPageNum:String):String?{
        var sResult:String?=null

        var sort:String = "sim" // sim:정확도순(기본), date:날짜순
        if(sSortType=="recency") sort = "date" // recency => date

        //페이지 번호를 시작번호로 변경. 화면에 기본 10개씩 보여주기 때문에 *10 + 1 함.
        val nPageNum = try{ sPageNum.toInt()*10+1 }catch (e:Exception){ 1 }
        val pageNum = nPageNum.toString()

        try{
            //api요청
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.set("X-Naver-Client-Id", idNaverApi)
            headers.set("X-Naver-Client-Secret", pwNaverApi)
            val uri = "$urlNaverBlogApi?query=$sQuery&sort=$sort&start=$pageNum"
            val entity = HttpEntity(null, headers)
            val response = restTemplate.exchange(uri, HttpMethod.GET, entity, HashMap::class.java)
            val temp:HashMap<*,*> = response.body ?: HashMap<String,Any>()

            //결과 오브젝트
            val result:HashMap<String,Any> = HashMap<String,Any>() //최종 결과
            val meta:HashMap<String,Any> = HashMap<String,Any>() //최종 meta
            meta["is_end"] = true
            meta["pageable_count"] = 0
            meta["total_count"] = 0
            result["meta"] = meta
            val documents:ArrayList<HashMap<String,Any>> = ArrayList<HashMap<String,Any>>() //최종 documents
            result["documents"] = documents

            //메타 작성
            val total = temp["total"] as Int
            val display = temp["display"] as Int
            val pageableCount = ceil(total.toDouble()/display).toInt()

            meta["total_count"] = total
            meta["pageable_count"] = pageableCount
            if(nPageNum<pageableCount) meta["is_end"] = false

            //documents작성
            var items:ArrayList<HashMap<String,Any>> = temp["items"] as ArrayList<HashMap<String,Any>>
            var document:HashMap<String,Any>
            var postdate:String
            for (item in items) {
                document = HashMap<String,Any>()
                document["blogname"] = item["bloggername"] as String
                document["contents"] = item["description"] as String
                postdate = item["postdate"] as String
                document["datetime"] = "${postdate.substring(0, 4)}-${postdate.substring(4, 6)}-${postdate.substring(6)}T00:00:00+09:00"
                document["thumbnail"] = ""
                document["title"] = item["title"] as String
                document["url"] = item["link"] as String
                documents.add(document)
            }

            val mapper = jacksonObjectMapper()
            sResult = mapper.writeValueAsString(result)
        }catch (e:Exception){ println(e.toString()) }

        return sResult
    }





    //========================================================================
    // 1. 인기 검색어 10개 검색.
    // 3. Param :
    // 4. 설명 :
    // 5. 작성 : bk.choi(2023-03-22)
    // 6. 수정 :
    //========================================================================
    fun getTop10Keywords(): List<Keyword> = keywordRepository.findTop10ByOrderByKeywordCntDesc()



    //========================================================================
    // 1. ChatGpt를 이용해 연관검색어 검색
    // 3. Param : sQuery
    // 4. 설명 : 5분 이내 검색한 결과가 DB에 임시저장 되어있으면 가져오고
    //          없으면 API를 호출한다.
    // 5. 작성 : bk.choi(2023-03-22)
    // 6. 수정 :
    //========================================================================
    fun askRelatedKeyword(sQuery:String):List<String>{
        val mapper = jacksonObjectMapper()

        //먼저 DB에서 최근 검색 결과가 있는지 확인
        val relatedKeyword = getRelatedKeyword(sQuery)

        val nTimestamp = Instant.now().toEpochMilli()

        var sJson:String?=null
        if(nTimestamp < relatedKeyword.timestamp+ 5*60*1000){
            //검색결과가 일정 시간 이내면 사용
            sJson = relatedKeyword.result
        }else{
            sJson = askChatGpt("Please provide 5 related search keywords that people frequently search for regarding the keyword '$sQuery'. Your answers should be in Korean and should not contain any line breaks or tabs. Additionally, do not include any ordering in your answers.")
            if(sJson===null) return List(0){""}
        }

        //검색결과 저장(현재시간으로)
        relatedKeywordRepository.save(RelatedKeyword(sQuery, nTimestamp, sJson))

        return mapper.readValue(sJson, object : TypeReference<List<String>>() {})
    }



    //========================================================================
    // 1. 임시 저장 결과 가져오기 함수.
    // 3. Param : sParam
    // 4. 설명 : DB에서 임시저장된 검색결과를 가져오는 함수.
    // 5. 작성 : bk.choi(2023-03-22)
    // 6. 수정 :
    //========================================================================
    @Transactional
    private fun getRelatedKeyword(sKeyword:String): RelatedKeyword{
        return relatedKeywordRepository.findById(sKeyword)
                .orElse(RelatedKeyword(sKeyword, -1L, "")).also { relatedKeywordRepository.save(it) }
    }


    //========================================================================
    // 1. OpenAi API 호출 함수.
    // 3. Param : sQuery, sSortType, sPageNum
    // 4. 설명 : 카카오 블로그 API 호출 함수.
    // 5. 작성 : bk.choi(2023-03-22)
    // 6. 수정 :
    //========================================================================
    private fun askChatGpt(sQuery:String):String?{
        var sResult:String?=null
        try{
            val mapper = jacksonObjectMapper()

            //요청에 필요한 바디부분 만들기
            val body:HashMap<String,Any> = HashMap<String, Any>()
            body["model"] = "gpt-3.5-turbo";
            body["temperature"] = 0.7;
            val messages:ArrayList<HashMap<String,Any>> = ArrayList<HashMap<String,Any>>()
            var message:HashMap<String, Any> = HashMap<String,Any>()
            message["role"] = "user"
            message["content"] = sQuery
            messages.add(message)
            body["messages"] = messages

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.set("Authorization", "Bearer $keyOpenAiApi")
            val uri = urlOpenAiApi
            val requestBody = mapper.writeValueAsString(body)
            val entity = HttpEntity(requestBody, headers)
            val response = restTemplate.exchange(uri, HttpMethod.POST, entity, HashMap::class.java)
            val temp:HashMap<*,*> = response.body ?: HashMap<String,Any>()

            try { //응답 받은 choices[0].message.content 꺼내기
                val choices = temp["choices"] as ArrayList<HashMap<String, Any>>
                val choice = choices.get(0) as HashMap<String,Any>
                val message = choice["message"] as HashMap<String, Any>
                val content = message["content"] as String

                //줄바꿈이나 순번 지우기
                val str:String = Regex("[\n\t1-5.]+").replace(content, "").trim()
                val arr = str.split(",\\s*".toRegex()) //배열 채우기

                if(arr.size===5) sResult = mapper.writeValueAsString(arr)
            }catch (e:Exception){ println(e.toString()) }

        }catch (e:Exception){ println(e.toString()) }

        return sResult
    }


}