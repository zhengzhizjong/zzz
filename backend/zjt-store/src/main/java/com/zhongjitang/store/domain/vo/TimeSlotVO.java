package com.zhongjitang.store.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "时段VO")
public class TimeSlotVO {

    @Schema(description = "时段", example = "09:00")
    private String timeSlot;

    @Schema(description = "是否可用", example = "true")
    private Boolean available;

    @Schema(description = "剩余数量", example = "5")
    private Integer remainingCount;
}
