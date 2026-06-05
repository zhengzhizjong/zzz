package com.zhongjitang.store.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.StoreCreateRequest;
import com.zhongjitang.store.domain.dto.StoreUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreInfoDO;
import com.zhongjitang.store.service.IStoreInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/store/stores")
@Tag(name = "门店管理")
@RequiredArgsConstructor
public class StoreInfoController {

    private final IStoreInfoService storeInfoService;

    @PostMapping
    @Operation(summary = "创建门店")
    public R<Void> create(@Valid @RequestBody StoreCreateRequest request) {
        return storeInfoService.create(request);
    }

    @GetMapping
    @Operation(summary = "门店列表")
    public R<PageResult<StoreInfoDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return storeInfoService.page(page, pageSize, keyword, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "门店详情")
    public R<StoreInfoDO> getById(@PathVariable Long id) {
        return storeInfoService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新门店")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody StoreUpdateRequest request) {
        return storeInfoService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "状态变更")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return storeInfoService.updateStatus(id, status);
    }
}
