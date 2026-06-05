package com.zhongjitang.billing.controller;

import com.zhongjitang.billing.domain.dto.TenantCreateRequest;
import com.zhongjitang.billing.domain.entity.BillingTenantDO;
import com.zhongjitang.billing.domain.vo.TenantVO;
import com.zhongjitang.billing.service.IBillingTenantService;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/tenants")
@Tag(name = "租户管理")
@RequiredArgsConstructor
public class BillingTenantController {

    private final IBillingTenantService billingTenantService;

    @GetMapping
    @Operation(summary = "租户列表")
    public R<PageResult<BillingTenantDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return billingTenantService.page(page, pageSize, keyword, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "租户详情")
    public R<TenantVO> getById(@PathVariable Long id) {
        return billingTenantService.getById(id);
    }

    @PostMapping
    @Operation(summary = "创建租户")
    public R<Void> create(@Valid @RequestBody TenantCreateRequest request) {
        return billingTenantService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新租户")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody TenantCreateRequest request) {
        return billingTenantService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "状态变更")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return billingTenantService.updateStatus(id, status);
    }

    @PutMapping("/{id}/white-label")
    @Operation(summary = "白标配置")
    public R<Void> updateWhiteLabel(
            @PathVariable Long id,
            @RequestParam(required = false) String logoUrl,
            @RequestParam(required = false) String primaryColor,
            @RequestParam(required = false) String customDomain) {
        return billingTenantService.updateWhiteLabel(id, logoUrl, primaryColor, customDomain);
    }
}
