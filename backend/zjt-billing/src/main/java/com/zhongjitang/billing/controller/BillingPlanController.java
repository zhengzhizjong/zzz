package com.zhongjitang.billing.controller;

import com.zhongjitang.billing.domain.dto.BillingPlanCreateRequest;
import com.zhongjitang.billing.domain.entity.BillingPlanDO;
import com.zhongjitang.billing.service.IBillingPlanService;
import com.zhongjitang.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/plans")
@Tag(name = "套餐管理")
@RequiredArgsConstructor
public class BillingPlanController {

    private final IBillingPlanService billingPlanService;

    @GetMapping
    @Operation(summary = "套餐列表")
    public R<List<BillingPlanDO>> list() {
        return billingPlanService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "套餐详情")
    public R<BillingPlanDO> getById(@PathVariable Long id) {
        return billingPlanService.getById(id);
    }

    @PostMapping
    @Operation(summary = "创建套餐")
    public R<Void> create(@Valid @RequestBody BillingPlanCreateRequest request) {
        return billingPlanService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新套餐")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody BillingPlanCreateRequest request) {
        return billingPlanService.update(id, request);
    }
}
