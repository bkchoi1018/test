package com.example.test.repository

import com.example.test.entity.Keyword
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface KeywordRepository : JpaRepository<Keyword, String> {
    fun findTop10ByOrderByKeywordCntDesc(): List<Keyword>
}
