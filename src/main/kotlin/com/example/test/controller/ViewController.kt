package com.example.test.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model

@Controller
class ViewController {

    @GetMapping("/")
    fun root(model: Model): String {

        //브라우저 클라이언트에서 js 사용할 변수.
        val map:HashMap<String, Any> = HashMap<String,Any>()
        map.put("sTitle", "블로그_검색")
        map.put("sViewAddr", "http://127.0.0.1:8080")
        map.put("sApiAddr", "http://127.0.0.1:8080")

        model.addAttribute("json_data", map)
        return "View_main"
    }
}