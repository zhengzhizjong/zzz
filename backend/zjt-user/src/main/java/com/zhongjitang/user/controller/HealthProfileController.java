package com.zhongjitang.user.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.HealthProfileRequest;
import com.zhongjitang.user.domain.entity.UserHealthProfileDO;
import com.zhongjitang.user.service.IHealthProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user/health-profiles")
@Tag(name = "健康档案")
@RequiredArgsConstructor
public class HealthProfileController {

    private final IHealthProfileService healthProfileService;

    @GetMapping("/{memberId}")
    @Operation(summary = "查询健康档案")
    public R<UserHealthProfileDO> getByMemberId(@PathVariable Long memberId) {
        return healthProfileService.getByMemberId(memberId);
    }

    @PostMapping
    @Operation(summary = "创建/更新健康档案")
    public R<Void> saveOrUpdate(@Valid @RequestBody HealthProfileRequest request) {
        return healthProfileService.saveOrUpdate(request);
    }
}
