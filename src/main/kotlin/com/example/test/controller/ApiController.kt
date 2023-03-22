package com.example.test.controller

import com.example.test.service.SearchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class ApiController(
        private val searchService: SearchService
){


    @GetMapping("/api/blogs")
    fun blogs(
            @RequestParam sApiType:String
            , @RequestParam sQuery:String
            , @RequestParam(required = false) sSort:String
            , @RequestParam(required = false) sPageNum:String?
    ):HashMap<String, Any> {
        val res = HashMap<String,Any>() //응답할 오브젝트
        res["bStatus"] = false //성공시 최후에 true로 변경

        val apiType:String = sApiType.takeIf { it=="naver" } ?: "kakao" // 기본:kakao
        res["sApiType"] = apiType

        res["sQuery"] = sQuery

        val sortType:String = sSort.takeIf { it=="recency" } ?: "accuracy" // 기본:정확도순
        res["sSort"] = sortType
        res["sPageNum"] = sPageNum?:"1"

        //검색 버튼 눌렀을때 검색카운트 증가, 페이징넘버는 카운트시키지않음.
        if(sPageNum===null || sPageNum.isBlank()) searchService.increaseKeywordCount(sQuery)

        //검색 결과
        res["bSubApi"] = false //대체 검색소스 사용 여부
        var result:HashMap<String, Any>? = searchService.askBlog(apiType, sQuery, sortType, sPageNum?:"1")

        if(result===null){
            if(apiType=="kakao") result = searchService.askBlog("naver", sQuery, sortType, sPageNum?:"1")
            else if(apiType=="naver") result = searchService.askBlog("kakao", sQuery, sortType, sPageNum?:"1")

            if(result===null) return res
            else res["bSubApi"] = true
        }

        res["result"] = result

        //탑10 키워드
        res["topKeywords"] = searchService.getTop10Keywords()

        res["bStatus"] = true
        return res
    }



    @GetMapping("/api/keyword")
    fun keyword(@RequestParam sQuery:String):HashMap<String, Any> {
        val res = HashMap<String,Any>() //응답할 오브젝트
        res["bStatus"] = false //성공시 최후에 true로 변경
        res["sQuery"] = sQuery

        //연관 검색어
        res["relatedKeywords"] = searchService.askRelatedKeyword(sQuery)

        res["bStatus"] = true
        return res
    }
}