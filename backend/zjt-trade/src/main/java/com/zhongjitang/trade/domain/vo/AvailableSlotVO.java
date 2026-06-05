package com.zhongjitang.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "可用时段VO")
public class AvailableSlotVO {

    @Schema(description = "门店ID", example = "1")
    private Long storeId;

    @Schema(description = "日期", example = "2026-06-05")
    private LocalDate date;

    @Schema(description = "技师ID，0表示不指定", example = "1")
    private Long technicianId;

    @Schema(description = "时段列表")
    private List<TimeSlotItem> timeSlots;

    @Data
    @Schema(description = "时段项")
    public static class TimeSlotItem {

        @Schema(description = "时段", example = "09:00")
        private String timeSlot;

        @Schema(description = "是否可用", example = "true")
        private Boolean available;

        @Schema(description = "剩余数量", example = "5")
        private Integer remainingCount;
    }
}
