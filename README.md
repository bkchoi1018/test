# test 
최부관.



1.	빌드 결과물
  https://drive.google.com/file/d/1l_sVEGrlZvRNCvAXq2_nrUzFUeLBb3YE/view?usp=share_link

  실행 : java -jar kakaobank.jar
  
  실행후 접속 주소 : http://127.0.0.1:8080/
  



2.	추가 구현기능

 A.	연관검색어 기능 추가(/api/keyword)
  
  i.	질의어에 대한 OpenAi Api를 사용하여 최근 이슈가되고 있는 ChatGpt 모델을 이용해 질의어에 대한 연관검색어를 추천받아 응답해줌. 
  
  ii.	자세한 내용은 API명세 참조
 
 *** openai api key를 깃에 퍼블릭으로 올리게될경우 api key가 자동으로 사용불가됩니다. 테스트를 위해서 src\main\kotlin\com\example\test\config\ApiConfig.kt 
 파일의 fun keyOpenAiApi():String = "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" 값을 
  "sk-mW4EnvN227tEagLHZzTGT3BlbkFJWZweiIHhCH육ntOOu0k02" 로 변경해서 실행부탁드립니다.(육 대신 6 으로)
  
  실행가능파일은 사용할 수 있는 다른api키로 넣어놨습니다.



3.	외부라이브러리 및 오픈소스 사용 목적 명시
 
 A.	서버 
  
  i.	org.springframework.boot:spring-boot-starter-data-jpa
   
   - JPA 구현체를 사용하기 위함
  
  ii.	org.springframework.boot:spring-boot-starter-web"
   
   - 스프링부트 웹어플리케이션을 만들기 위함.
  
  iii.	org.springframework.boot:spring-boot-starter-thymeleaf
   
   -	뷰 페이지를 위한 타임리프 템플릿 엔진을 사용하기 위함
  
  iv.	com.fasterxml.jackson.module:jackson-module-kotlin
   
   -	JSON 파씽 및 문자열을 만들기 위함
  
  v.	org.jetbrains.kotlin:kotlin-reflect
   
   -	코틀린에서 리플렉션 API를 사용하기 위함
  
  vi.	com.h2database:h2
   
   -	과제조건에 제시된 인메모리 디비를 사용하기 위함.
  
  vii.	org.springframework.boot:spring-boot-starter-test
   
   -	스프링 부트 기반 어플리케이션 테스트를 위함
 
 B.	클라이언트(사용자 페이지)
  
  i.	jquery-2.2.4.min.js
   
   -	Jquery 사용을 위함
  
  ii.	https://fonts.google.com/icons
   
   -	구글 아이콘 등을 사용하기 위함.






