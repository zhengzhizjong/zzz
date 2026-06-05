package com.zhongjitang.store.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.ScheduleBatchCreateRequest;
import com.zhongjitang.store.domain.dto.ScheduleCreateRequest;
import com.zhongjitang.store.domain.dto.ScheduleUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreScheduleDO;
import com.zhongjitang.store.service.IScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/store/schedules")
@Tag(name = "排班管理")
@RequiredArgsConstructor
public class ScheduleController {

    private final IScheduleService scheduleService;

    @GetMapping("/technician/{technicianId}")
    @Operation(summary = "技师排班")
    public R<List<StoreScheduleDO>> getByTechnicianAndMonth(
            @PathVariable Long technicianId,
            @RequestParam String month) {
        return scheduleService.getByTechnicianAndMonth(technicianId, month);
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "门店排班")
    public R<List<StoreScheduleDO>> getByStoreAndDate(
            @PathVariable Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.getByStoreAndDate(storeId, date);
    }

    @PostMapping
    @Operation(summary = "创建排班")
    public R<Void> create(@Valid @RequestBody ScheduleCreateRequest request) {
        return scheduleService.create(request);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量排班")
    public R<Void> batchCreate(@Valid @RequestBody ScheduleBatchCreateRequest request) {
        return scheduleService.batchCreate(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新排班")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ScheduleUpdateRequest request) {
        return scheduleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除排班")
    public R<Void> delete(@PathVariable Long id) {
        return scheduleService.delete(id);
    }
}
