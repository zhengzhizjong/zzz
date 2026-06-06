package com.zhongjitang.integration.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.integration.domain.dto.ConfigCreateRequest;
import com.zhongjitang.integration.domain.dto.ConfigUpdateRequest;
import com.zhongjitang.integration.domain.entity.IntegrationConfigDO;
import com.zhongjitang.integration.service.IIntegrationConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/integration/configs")
@Tag(name = "集成配置管理")
@RequiredArgsConstructor
public class IntegrationConfigController {

    private final IIntegrationConfigService integrationConfigService;

    @GetMapping
    @Operation(summary = "配置列表")
    public R<List<IntegrationConfigDO>> list() {
        return integrationConfigService.list();
    }

    @GetMapping("/platform/{platform}")
    @Operation(summary = "按平台查询配置")
    public R<IntegrationConfigDO> getByPlatform(@PathVariable String platform) {
        return integrationConfigService.getByPlatform(platform);
    }

    @PostMapping
    @Operation(summary = "创建配置")
    public R<Void> create(@Valid @RequestBody ConfigCreateRequest request) {
        return integrationConfigService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新配置")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ConfigUpdateRequest request) {
        return integrationConfigService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启停配置")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return integrationConfigService.updateStatus(id, status);
    }
}
