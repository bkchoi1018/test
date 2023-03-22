package com.example.test.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

//블로그 검색결과 임시저장.
@Entity
data class Search (
        @Id
        val param:String
        , var timestamp:Long
        , @Column(columnDefinition = "TEXT") var result:String
)

