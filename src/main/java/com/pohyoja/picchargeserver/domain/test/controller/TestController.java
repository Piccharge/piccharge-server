package com.pohyoja.picchargeserver.domain.test.controller;

import com.pohyoja.picchargeserver.common.BaseResponse;
import com.pohyoja.picchargeserver.common.security.JwtUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public BaseResponse<JwtUserDetails> test(@AuthenticationPrincipal JwtUserDetails userDetails) {
        return BaseResponse.onSuccess(userDetails);
    }
}
