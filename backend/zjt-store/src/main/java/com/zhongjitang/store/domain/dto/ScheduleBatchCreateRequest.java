package com.zhongjitang.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Schema(description = "批量排班请求")
public class ScheduleBatchCreateRequest {

    @NotEmpty(message = "技师ID列表不能为空")
    @Schema(description = "技师ID列表")
    private List<Long> technicianIds;

    @NotNull(message = "开始日期不能为空")
    @Schema(description = "开始日期", example = "2026-06-05")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    @Schema(description = "结束日期", example = "2026-06-11")
    private LocalDate endDate;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", example = "09:00")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", example = "18:00")
    private LocalTime endTime;

    @NotNull(message = "排班类型不能为空")
    @Schema(description = "排班类型: 1上班 2休息 3请假", example = "1")
    private Integer scheduleType;

    @Schema(description = "班次: morning早班, afternoon中班, evening晚班", example = "morning")
    private String shiftType;

    @Schema(description = "休息日(0-6，0=周日)", example = "[0,6]")
    private List<Integer> restDays;
}
