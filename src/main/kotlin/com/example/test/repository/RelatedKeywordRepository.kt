package com.example.test.repository

import com.example.test.entity.RelatedKeyword
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface  RelatedKeywordRepository : JpaRepository<RelatedKeyword, String> { }
