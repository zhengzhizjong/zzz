package com.zhongjitang.store.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "推广统计VO")
public class PromotionStatsVO {

    @Schema(description = "技师ID")
    private Long technicianId;

    @Schema(description = "统计周期")
    private Period period;

    @Schema(description = "统计指标")
    private Metrics metrics;

    @Schema(description = "每日趋势")
    private List<DailyTrend> dailyTrend;

    @Data
    @Schema(description = "统计周期")
    public static class Period {

        @Schema(description = "开始日期")
        private LocalDate startDate;

        @Schema(description = "结束日期")
        private LocalDate endDate;
    }

    @Data
    @Schema(description = "统计指标")
    public static class Metrics {

        @Schema(description = "总点击数")
        private Long totalClicks;

        @Schema(description = "总浏览数")
        private Long totalViews;

        @Schema(description = "总预约数")
        private Long totalAppointments;

        @Schema(description = "已完成预约数")
        private Long completedAppointments;

        @Schema(description = "转化率(百分比)")
        private Double conversionRate;

        @Schema(description = "总佣金")
        private BigDecimal totalCommission;
    }

    @Data
    @Schema(description = "每日趋势")
    public static class DailyTrend {

        @Schema(description = "日期")
        private LocalDate date;

        @Schema(description = "点击数")
        private Long clicks;

        @Schema(description = "浏览数")
        private Long views;

        @Schema(description = "预约数")
        private Long appointments;
    }
}
