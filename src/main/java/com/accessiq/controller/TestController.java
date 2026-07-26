package com.accessiq.controller;

import com.accessiq.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public ApiResponse<String> test() {
        return ApiResponse.success("AccessIQ backend is running");
    }
}