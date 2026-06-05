package com.zhongjitang.integration.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.integration.domain.dto.LeadCreateRequest;
import com.zhongjitang.integration.domain.dto.LeadUpdateRequest;
import com.zhongjitang.integration.domain.entity.IntegrationLeadDO;
import com.zhongjitang.integration.service.ILeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/integration/leads")
@Tag(name = "线索管理")
@RequiredArgsConstructor
public class LeadController {

    private final ILeadService leadService;

    @GetMapping
    @Operation(summary = "线索列表")
    public R<PageResult<IntegrationLeadDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) Integer status) {
        return leadService.page(page, pageSize, keyword, platform, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "线索详情")
    public R<IntegrationLeadDO> getById(@PathVariable Long id) {
        return leadService.getById(id);
    }

    @PostMapping
    @Operation(summary = "创建线索")
    public R<Void> create(@Valid @RequestBody LeadCreateRequest request) {
        return leadService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新线索")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody LeadUpdateRequest request) {
        return leadService.update(id, request);
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "分配")
    public R<Void> assign(@PathVariable Long id, @RequestParam Long assignedTo) {
        return leadService.assign(id, assignedTo);
    }

    @PutMapping("/{id}/convert")
    @Operation(summary = "转化")
    public R<Void> convert(@PathVariable Long id, @RequestParam Long memberId) {
        return leadService.convert(id, memberId);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "状态变更")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return leadService.updateStatus(id, status);
    }
}
