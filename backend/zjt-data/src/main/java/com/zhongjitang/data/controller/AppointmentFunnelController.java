package com.zhongjitang.data.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.data.domain.dto.FunnelTrackRequest;
import com.zhongjitang.data.domain.vo.FunnelStatisticsVO;
import com.zhongjitang.data.service.AppointmentFunnelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/data/appointment-funnel")
@RequiredArgsConstructor
@Tag(name = "预约漏斗")
public class AppointmentFunnelController {

    private final AppointmentFunnelService appointmentFunnelService;

    @PostMapping("/track")
    @Operation(summary = "埋点上报")
    public R<Void> track(@Valid @RequestBody FunnelTrackRequest request) {
        return appointmentFunnelService.track(request);
    }

    @GetMapping("/statistics")
    @Operation(summary = "漏斗统计")
    public R<FunnelStatisticsVO> getStatistics(
            @Parameter(description = "门店ID") @RequestParam(required = false) Long storeId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "分组维度: store, channel, technician, date") @RequestParam(required = false) String groupBy) {
        return appointmentFunnelService.getStatistics(storeId, startDate, endDate, groupBy);
    }
}
