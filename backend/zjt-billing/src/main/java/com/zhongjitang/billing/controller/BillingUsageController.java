package com.zhongjitang.billing.controller;

import com.zhongjitang.billing.domain.entity.BillingUsageDO;
import com.zhongjitang.billing.domain.vo.UsageSummaryVO;
import com.zhongjitang.billing.service.IBillingUsageService;
import com.zhongjitang.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/usage")
@Tag(name = "用量管理")
@RequiredArgsConstructor
public class BillingUsageController {

    private final IBillingUsageService billingUsageService;

    @GetMapping("/{tenantId}")
    @Operation(summary = "查询用量")
    public R<List<BillingUsageDO>> getUsage(
            @PathVariable Long tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return billingUsageService.getUsage(tenantId, startDate, endDate);
    }

    @GetMapping("/{tenantId}/summary")
    @Operation(summary = "用量汇总")
    public R<UsageSummaryVO> getSummary(
            @PathVariable Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        return billingUsageService.getSummary(tenantId, month);
    }
}
