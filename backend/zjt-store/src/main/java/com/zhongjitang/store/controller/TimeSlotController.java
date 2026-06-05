package com.zhongjitang.store.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.TimeSlotConfigCreateRequest;
import com.zhongjitang.store.domain.dto.TimeSlotConfigUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreTimeSlotConfigDO;
import com.zhongjitang.store.domain.vo.TimeSlotVO;
import com.zhongjitang.store.service.ITimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/store")
@Tag(name = "时段配置")
@RequiredArgsConstructor
public class TimeSlotController {

    private final ITimeSlotService timeSlotService;

    @PostMapping("/time-slot-config")
    @Operation(summary = "创建时段配置")
    public R<Void> create(@Valid @RequestBody TimeSlotConfigCreateRequest request) {
        return timeSlotService.create(request);
    }

    @GetMapping("/time-slot-config/{storeId}")
    @Operation(summary = "按门店查询配置")
    public R<StoreTimeSlotConfigDO> getByStoreId(@PathVariable Long storeId) {
        return timeSlotService.getByStoreId(storeId);
    }

    @PutMapping("/time-slot-config/{id}")
    @Operation(summary = "更新配置")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody TimeSlotConfigUpdateRequest request) {
        return timeSlotService.update(id, request);
    }

    @GetMapping("/time-slots/available")
    @Operation(summary = "查询可用时段")
    public R<List<TimeSlotVO>> getAvailableSlots(
            @RequestParam Long storeId,
            @RequestParam(defaultValue = "0") Long technicianId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return timeSlotService.getAvailableSlots(storeId, technicianId, date);
    }
}
