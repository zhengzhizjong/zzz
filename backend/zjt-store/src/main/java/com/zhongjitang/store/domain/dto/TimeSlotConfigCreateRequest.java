package com.zhongjitang.store.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalTime;

@Data
@Schema(description = "创建时段配置请求")
public class TimeSlotConfigCreateRequest {

    @NotNull(message = "门店ID不能为空")
    @Schema(description = "门店ID", example = "1")
    private Long storeId;

    @NotNull(message = "营业开始时间不能为空")
    @Schema(description = "营业开始时间", example = "09:00")
    private LocalTime businessStartTime;

    @NotNull(message = "营业结束时间不能为空")
    @Schema(description = "营业结束时间", example = "21:00")
    private LocalTime businessEndTime;

    @NotNull(message = "时段时长不能为空")
    @Schema(description = "时段时长(分钟)", example = "60")
    private Integer slotDurationMinutes;

    @Schema(description = "休息开始时间", example = "12:00")
    private LocalTime restStartTime;

    @Schema(description = "休息结束时间", example = "13:00")
    private LocalTime restEndTime;
}
