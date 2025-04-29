package com.pohyoja.picchargeserver.domain.test.controller;

import com.pohyoja.picchargeserver.global.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public BaseResponse<String> test() {
        return BaseResponse.onSuccess("test");
    }
}
