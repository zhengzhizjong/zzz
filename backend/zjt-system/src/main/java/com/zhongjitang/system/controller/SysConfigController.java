package com.zhongjitang.system.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.entity.SysConfigDO;
import com.zhongjitang.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/configs")
@RequiredArgsConstructor
@Tag(name = "系统配置")
public class SysConfigController {

    private final ISysConfigService sysConfigService;

    @GetMapping
    @Operation(summary = "配置列表")
    public R<List<SysConfigDO>> list() {
        return sysConfigService.list();
    }

    @GetMapping("/{key}")
    @Operation(summary = "按Key查询配置")
    public R<SysConfigDO> getByKey(@PathVariable String key) {
        return sysConfigService.getByKey(key);
    }

    @PutMapping("/{key}")
    @Operation(summary = "更新配置")
    public R<Void> update(
            @PathVariable String key,
            @Parameter(description = "配置值") @RequestParam String value) {
        return sysConfigService.update(key, value);
    }
}
