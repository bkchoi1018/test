package com.example.test.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

//인기 검색어
@Entity
data class Keyword (
        @Id
        val keyword:String
        , var keywordCnt:Int
)