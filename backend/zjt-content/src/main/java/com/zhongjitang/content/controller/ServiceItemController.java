package com.zhongjitang.content.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.content.domain.dto.ServiceItemCreateRequest;
import com.zhongjitang.content.domain.dto.ServiceItemUpdateRequest;
import com.zhongjitang.content.domain.entity.ContentServiceItemDO;
import com.zhongjitang.content.service.IServiceItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/service-items")
@RequiredArgsConstructor
@Tag(name = "服务项目管理")
public class ServiceItemController {

    private final IServiceItemService serviceItemService;

    @PostMapping
    @Operation(summary = "创建服务项目")
    public R<Void> create(@Valid @RequestBody ServiceItemCreateRequest request) {
        return serviceItemService.create(request);
    }

    @GetMapping
    @Operation(summary = "服务项目列表(分页)")
    public R<PageResult<ContentServiceItemDO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status,
            @Parameter(description = "分类ID筛选") @RequestParam(required = false) Long categoryId) {
        return serviceItemService.page(page, pageSize, keyword, status, categoryId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "服务项目详情")
    public R<ContentServiceItemDO> getById(@PathVariable Long id) {
        return serviceItemService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新服务项目")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ServiceItemUpdateRequest request) {
        return serviceItemService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "上下架服务项目")
    public R<Void> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "状态:1上架 2下架") @RequestParam Integer status) {
        return serviceItemService.updateStatus(id, status);
    }

    @GetMapping("/list")
    @Operation(summary = "全部上架项目(小程序端)")
    public R<List<ContentServiceItemDO>> listAll(
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status) {
        return serviceItemService.listAll(status);
    }
}
