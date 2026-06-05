package com.zhongjitang.trade.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.AppointmentCancelRequest;
import com.zhongjitang.trade.domain.dto.AppointmentCreateRequest;
import com.zhongjitang.trade.domain.dto.AppointmentModifyRequest;
import com.zhongjitang.trade.domain.dto.LockTempRequest;
import com.zhongjitang.trade.domain.entity.TradeAppointmentDO;
import com.zhongjitang.trade.domain.vo.AppointmentResponse;
import com.zhongjitang.trade.domain.vo.AvailableSlotVO;
import com.zhongjitang.trade.domain.vo.LockResultVO;
import com.zhongjitang.trade.domain.vo.MyAppointmentVO;
import com.zhongjitang.trade.service.AppointmentLockService;
import com.zhongjitang.trade.service.AppointmentSlotService;
import com.zhongjitang.trade.service.IAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/trade/appointments")
@Tag(name = "预约管理")
@RequiredArgsConstructor
public class AppointmentController {

    private final IAppointmentService appointmentService;
    private final AppointmentSlotService appointmentSlotService;
    private final AppointmentLockService appointmentLockService;

    @GetMapping("/available-slots")
    @Operation(summary = "查询可用时段")
    public R<AvailableSlotVO> getAvailableSlots(
            @RequestParam Long store_id,
            @RequestParam(defaultValue = "0") Long technician_id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentSlotService.getAvailableSlots(store_id, technician_id, date);
    }

    @PostMapping("/lock-temp")
    @Operation(summary = "临时锁定时段")
    public R<LockResultVO> lockTemp(@Valid @RequestBody LockTempRequest request) {
        // TODO: 从安全上下文获取当前会员ID，暂时传0
        Long memberId = 0L;
        return appointmentLockService.lockTemp(request, memberId);
    }

    @PostMapping
    @Operation(summary = "创建预约")
    public R<AppointmentResponse> create(@Valid @RequestBody AppointmentCreateRequest request) {
        return appointmentService.create(request);
    }

    @GetMapping
    @Operation(summary = "预约列表(管理端)")
    public R<PageResult<TradeAppointmentDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long store_id,
            @RequestParam(required = false) Long member_id,
            @RequestParam(required = false) Integer status) {
        return appointmentService.page(page, pageSize, store_id, member_id, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "预约详情")
    public R<TradeAppointmentDO> getById(@PathVariable Long id) {
        return appointmentService.getById(id);
    }

    @GetMapping("/my")
    @Operation(summary = "我的预约列表")
    public R<MyAppointmentVO> getMyAppointments(
            @RequestParam(defaultValue = "all") String statusGroup) {
        // TODO: 从安全上下文获取当前会员ID，暂时传0
        Long memberId = 0L;
        return appointmentService.getMyAppointments(memberId, statusGroup);
    }

    @PutMapping("/{id}/modify")
    @Operation(summary = "修改预约")
    public R<AppointmentResponse> modify(@PathVariable Long id,
                                          @Valid @RequestBody AppointmentModifyRequest request) {
        return appointmentService.modify(id, request);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消预约")
    public R<Void> cancel(@PathVariable Long id,
                           @Valid @RequestBody AppointmentCancelRequest request) {
        return appointmentService.cancel(id, request);
    }
}
