package com.example.test.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApiConfig {
    @Bean
    fun urlKakaoBlogApi():String="https://dapi.kakao.com/v2/search/blog"

    @Bean
    fun keyKakaoApi():String = "d3e0edc1cc4a36c29d5e67476f456d17"

    @Bean
    fun urlNaverBlogApi():String="https://openapi.naver.com/v1/search/blog.json"

    @Bean
    fun idNaverApi():String = "IeJd6bWIHAP9xeXSKJjU"

    @Bean
    fun pwNaverApi():String = "Y5BDBmehDc"

    @Bean
    fun urlOpenAiApi():String="https://api.openai.com/v1/chat/completions"

    @Bean
    fun keyOpenAiApi():String = "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"


}