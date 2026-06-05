package com.zhongjitang.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "更新排班请求")
public class ScheduleUpdateRequest {

    @Schema(description = "排班日期", example = "2026-06-05")
    private LocalDate scheduleDate;

    @Schema(description = "开始时间", example = "09:00")
    private LocalTime startTime;

    @Schema(description = "结束时间", example = "18:00")
    private LocalTime endTime;

    @Schema(description = "排班类型: 1上班 2休息 3请假", example = "1")
    private Integer scheduleType;

    @Schema(description = "备注")
    private String notes;
}
