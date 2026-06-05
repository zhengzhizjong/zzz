package com.zhongjitang.user.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.SendVerifyCodeRequest;
import com.zhongjitang.user.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user/sms")
@Tag(name = "短信验证码")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/send")
    @Operation(summary = "发送验证码")
    public R<Void> send(@Valid @RequestBody SendVerifyCodeRequest request) {
        smsService.sendVerifyCode(request.getPhone());
        return R.ok();
    }
}
