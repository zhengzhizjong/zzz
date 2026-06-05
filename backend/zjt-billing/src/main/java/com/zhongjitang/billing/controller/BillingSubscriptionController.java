package com.zhongjitang.billing.controller;

import com.zhongjitang.billing.domain.entity.BillingSubscriptionDO;
import com.zhongjitang.billing.service.IBillingSubscriptionService;
import com.zhongjitang.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@Tag(name = "订阅管理")
@RequiredArgsConstructor
public class BillingSubscriptionController {

    private final IBillingSubscriptionService billingSubscriptionService;

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "查询租户订阅")
    public R<BillingSubscriptionDO> getByTenantId(@PathVariable Long tenantId) {
        return billingSubscriptionService.getByTenantId(tenantId);
    }

    @PostMapping("/subscribe")
    @Operation(summary = "订阅套餐")
    public R<Void> subscribe(@RequestParam Long tenantId, @RequestParam Long planId) {
        return billingSubscriptionService.subscribe(tenantId, planId);
    }

    @PostMapping("/renew/{tenantId}")
    @Operation(summary = "续费")
    public R<Void> renew(@PathVariable Long tenantId) {
        return billingSubscriptionService.renew(tenantId);
    }

    @PostMapping("/cancel/{tenantId}")
    @Operation(summary = "取消")
    public R<Void> cancel(@PathVariable Long tenantId) {
        return billingSubscriptionService.cancel(tenantId);
    }
}
