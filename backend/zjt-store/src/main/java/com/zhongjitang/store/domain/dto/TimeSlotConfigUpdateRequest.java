package com.zhongjitang.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

@Data
@Schema(description = "更新时段配置请求")
public class TimeSlotConfigUpdateRequest {

    @Schema(description = "营业开始时间", example = "09:00")
    private LocalTime businessStartTime;

    @Schema(description = "营业结束时间", example = "21:00")
    private LocalTime businessEndTime;

    @Schema(description = "时段时长(分钟)", example = "60")
    private Integer slotDurationMinutes;

    @Schema(description = "休息开始时间", example = "12:00")
    private LocalTime restStartTime;

    @Schema(description = "休息结束时间", example = "13:00")
    private LocalTime restEndTime;
}
