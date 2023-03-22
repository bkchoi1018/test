package com.example.test.repository

import com.example.test.entity.Search
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface  SearchRepository : JpaRepository<Search, String> { }
