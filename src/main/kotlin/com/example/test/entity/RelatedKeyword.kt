package com.example.test.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

//연관 검색어 임시저장
@Entity
data class RelatedKeyword (
        @Id
        val keyword:String
        , var timestamp:Long
        , @Column(columnDefinition = "TEXT") var result:String
)

