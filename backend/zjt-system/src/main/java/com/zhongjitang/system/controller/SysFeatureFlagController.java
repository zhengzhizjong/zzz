package com.zhongjitang.system.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.FeatureFlagCreateRequest;
import com.zhongjitang.system.domain.dto.FeatureFlagUpdateRequest;
import com.zhongjitang.system.domain.entity.SysFeatureFlagDO;
import com.zhongjitang.system.service.ISysFeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/feature-flags")
@RequiredArgsConstructor
@Tag(name = "功能开关")
public class SysFeatureFlagController {

    private final ISysFeatureFlagService sysFeatureFlagService;

    @GetMapping
    @Operation(summary = "开关列表")
    public R<List<SysFeatureFlagDO>> list() {
        return sysFeatureFlagService.list();
    }

    @GetMapping("/{key}")
    @Operation(summary = "按Key查询开关")
    public R<SysFeatureFlagDO> getByKey(@PathVariable String key) {
        return sysFeatureFlagService.getByKey(key);
    }

    @PostMapping
    @Operation(summary = "创建开关")
    public R<Void> create(@Valid @RequestBody FeatureFlagCreateRequest request) {
        return sysFeatureFlagService.create(request);
    }

    @PutMapping("/{key}")
    @Operation(summary = "更新开关")
    public R<Void> update(@PathVariable String key, @Valid @RequestBody FeatureFlagUpdateRequest request) {
        return sysFeatureFlagService.update(key, request);
    }

    @GetMapping("/{key}/check")
    @Operation(summary = "检查功能开关是否启用")
    public R<Boolean> isEnabled(
            @PathVariable String key,
            @Parameter(description = "租户ID") @RequestParam(required = false) Long tenantId) {
        return sysFeatureFlagService.isEnabled(key, tenantId);
    }
}
