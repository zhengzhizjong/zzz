package com.zhongjitang.store.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.TechnicianCreateRequest;
import com.zhongjitang.store.domain.dto.TechnicianUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreTechnicianDO;
import com.zhongjitang.store.domain.vo.TechnicianPerformanceVO;
import com.zhongjitang.store.domain.vo.TechnicianWorkspaceVO;
import com.zhongjitang.store.service.IStoreTechnicianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/store/technicians")
@Tag(name = "技师管理")
@RequiredArgsConstructor
public class StoreTechnicianController {

    private final IStoreTechnicianService storeTechnicianService;

    @PostMapping
    @Operation(summary = "创建技师")
    public R<Void> create(@Valid @RequestBody TechnicianCreateRequest request) {
        return storeTechnicianService.create(request);
    }

    @GetMapping
    @Operation(summary = "技师列表")
    public R<PageResult<StoreTechnicianDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isOnline,
            @RequestParam(required = false) Integer skillLevel,
            @RequestParam(required = false) String skilledItems) {
        return storeTechnicianService.page(page, pageSize, storeId, status, isOnline, skillLevel, skilledItems);
    }

    @GetMapping("/{id}")
    @Operation(summary = "技师详情")
    public R<StoreTechnicianDO> getById(@PathVariable Long id) {
        return storeTechnicianService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新技师")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody TechnicianUpdateRequest request) {
        return storeTechnicianService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "状态变更")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return storeTechnicianService.updateStatus(id, status);
    }

    @PostMapping("/{id}/check-in")
    @Operation(summary = "签到")
    public R<Void> checkIn(@PathVariable Long id) {
        return storeTechnicianService.checkIn(id);
    }

    @PostMapping("/{id}/check-out")
    @Operation(summary = "签退")
    public R<Void> checkOut(@PathVariable Long id) {
        return storeTechnicianService.checkOut(id);
    }

    @GetMapping("/{id}/workspace")
    @Operation(summary = "技师工作台")
    public R<TechnicianWorkspaceVO> getWorkspace(@PathVariable Long id) {
        return storeTechnicianService.getWorkspace(id);
    }

    @GetMapping("/{id}/performance")
    @Operation(summary = "技师业绩")
    public R<TechnicianPerformanceVO> getPerformance(@PathVariable Long id) {
        return storeTechnicianService.getPerformance(id);
    }
}
