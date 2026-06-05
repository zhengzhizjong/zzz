package com.zhongjitang.store.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.StoreRoomCreateRequest;
import com.zhongjitang.store.domain.dto.StoreRoomUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreRoomDO;
import com.zhongjitang.store.service.IStoreRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/store/rooms")
@Tag(name = "房间管理")
@RequiredArgsConstructor
public class StoreRoomController {

    private final IStoreRoomService storeRoomService;

    @GetMapping
    @Operation(summary = "房间列表")
    public R<PageResult<StoreRoomDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Integer roomType,
            @RequestParam(required = false) Integer status) {
        return storeRoomService.page(page, pageSize, storeId, roomType, status);
    }

    @GetMapping("/available")
    @Operation(summary = "空闲房间")
    public R<List<StoreRoomDO>> getAvailableRooms(@RequestParam Long storeId) {
        return storeRoomService.getAvailableRooms(storeId);
    }

    @PostMapping
    @Operation(summary = "创建房间")
    public R<Void> create(@Valid @RequestBody StoreRoomCreateRequest request) {
        return storeRoomService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新房间")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody StoreRoomUpdateRequest request) {
        return storeRoomService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "状态变更")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return storeRoomService.updateStatus(id, status);
    }
}
