package com.zhongjitang.system.controller;

import com.zhongjitang.common.core.result.PageResult;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/feature-flags")
@RequiredArgsConstructor
@Tag(name = "功能开关")
public class SysFeatureFlagController {

    private final ISysFeatureFlagService sysFeatureFlagService;

    @GetMapping
    @Operation(summary = "开关列表(分页)")
    public R<PageResult<SysFeatureFlagDO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword) {
        return sysFeatureFlagService.page(page, pageSize, keyword);
    }

    @GetMapping("/{id}")
    @Operation(summary = "按ID查询开关")
    public R<SysFeatureFlagDO> getById(@PathVariable Long id) {
        return sysFeatureFlagService.getById(id);
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "按Key查询开关")
    public R<SysFeatureFlagDO> getByKey(@PathVariable String key) {
        return sysFeatureFlagService.getByKey(key);
    }

    @PostMapping
    @Operation(summary = "创建开关")
    public R<Void> create(@Valid @RequestBody FeatureFlagCreateRequest request) {
        return sysFeatureFlagService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "按ID更新开关")
    public R<Void> updateById(@PathVariable Long id, @Valid @RequestBody FeatureFlagUpdateRequest request) {
        return sysFeatureFlagService.updateById(id, request);
    }

    @PutMapping("/{id}/toggle")
    @Operation(summary = "切换开关状态")
    public R<Void> toggle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Boolean enabled = null;
        if (body.get("enabled") != null) {
            enabled = Boolean.valueOf(body.get("enabled").toString());
        }
        return sysFeatureFlagService.toggle(id, enabled);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除开关")
    public R<Void> delete(@PathVariable Long id) {
        return sysFeatureFlagService.delete(id);
    }

    @GetMapping("/key/{key}/check")
    @Operation(summary = "检查功能开关是否启用")
    public R<Boolean> isEnabled(
            @PathVariable String key,
            @Parameter(description = "租户ID") @RequestParam(required = false) Long tenantId) {
        return sysFeatureFlagService.isEnabled(key, tenantId);
    }
}
