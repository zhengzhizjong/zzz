package com.zhongjitang.system.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.SysDictCreateRequest;
import com.zhongjitang.system.domain.dto.SysDictItemCreateRequest;
import com.zhongjitang.system.domain.dto.SysDictItemUpdateRequest;
import com.zhongjitang.system.domain.dto.SysDictUpdateRequest;
import com.zhongjitang.system.domain.entity.SysDictDO;
import com.zhongjitang.system.domain.entity.SysDictItemDO;
import com.zhongjitang.system.service.ISysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dicts")
@RequiredArgsConstructor
@Tag(name = "字典管理")
public class SysDictController {

    private final ISysDictService sysDictService;

    @GetMapping
    @Operation(summary = "字典列表")
    public R<PageResult<SysDictDO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword) {
        return sysDictService.page(page, pageSize, keyword);
    }

    @GetMapping("/{id}")
    @Operation(summary = "字典详情")
    public R<SysDictDO> getById(@PathVariable Long id) {
        return sysDictService.getById(id);
    }

    @PostMapping
    @Operation(summary = "创建字典")
    public R<Void> create(@Valid @RequestBody SysDictCreateRequest request) {
        return sysDictService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新字典")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SysDictUpdateRequest request) {
        return sysDictService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除字典")
    public R<Void> delete(@PathVariable Long id) {
        return sysDictService.delete(id);
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "字典项列表")
    public R<List<SysDictItemDO>> getItems(@PathVariable Long id) {
        return sysDictService.getItems(id);
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "添加字典项")
    public R<Void> addItem(@PathVariable Long id, @RequestBody SysDictItemCreateRequest request) {
        request.setDictId(id);
        return sysDictService.addItem(request);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "更新字典项")
    public R<Void> updateItem(@PathVariable Long itemId, @Valid @RequestBody SysDictItemUpdateRequest request) {
        return sysDictService.updateItem(itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "删除字典项")
    public R<Void> deleteItem(@PathVariable Long itemId) {
        return sysDictService.deleteItem(itemId);
    }

    @GetMapping("/code/{dictCode}/items")
    @Operation(summary = "按编码查询字典项")
    public R<List<SysDictItemDO>> getItemsByCode(@PathVariable String dictCode) {
        return sysDictService.getItemsByCode(dictCode);
    }
}
