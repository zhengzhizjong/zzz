package com.zhongjitang.system.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.entity.SysConfigDO;
import com.zhongjitang.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/configs")
@RequiredArgsConstructor
@Tag(name = "系统配置")
public class SysConfigController {

    private final ISysConfigService sysConfigService;

    @GetMapping
    @Operation(summary = "配置列表(分页)")
    public R<PageResult<SysConfigDO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword) {
        return sysConfigService.page(page, pageSize, keyword);
    }

    @GetMapping("/{id}")
    @Operation(summary = "按ID查询配置")
    public R<SysConfigDO> getById(@PathVariable Long id) {
        return sysConfigService.getById(id);
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "按Key查询配置")
    public R<SysConfigDO> getByKey(@PathVariable String key) {
        return sysConfigService.getByKey(key);
    }

    @PutMapping("/{id}")
    @Operation(summary = "按ID更新配置")
    public R<Void> updateById(@PathVariable Long id, @Valid @RequestBody Map<String, Object> body) {
        String configValue = body.get("configValue") != null ? body.get("configValue").toString() : null;
        return sysConfigService.updateById(id, configValue);
    }
}
