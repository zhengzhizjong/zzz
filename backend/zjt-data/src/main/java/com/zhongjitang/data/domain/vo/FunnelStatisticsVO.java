package com.zhongjitang.data.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "漏斗统计结果")
public class FunnelStatisticsVO {

    @Schema(description = "统计周期")
    private Period period;

    @Schema(description = "漏斗各步骤数据，key为步骤名")
    private Map<String, FunnelStep> funnel;

    @Schema(description = "转化率，key为转换率名称，value为百分比")
    private Map<String, Double> conversion;

    @Schema(description = "按维度分组统计")
    private List<FunnelGroup> groups;

    @Data
    @Schema(description = "统计周期")
    public static class Period {

        @Schema(description = "开始日期")
        private LocalDate startDate;

        @Schema(description = "结束日期")
        private LocalDate endDate;
    }

    @Data
    @Schema(description = "漏斗步骤统计")
    public static class FunnelStep {

        @Schema(description = "数量")
        private Long count;

        @Schema(description = "相对第一步的转化率(百分比)")
        private Double rate;
    }

    @Data
    @Schema(description = "漏斗分组统计")
    public static class FunnelGroup {

        @Schema(description = "分组维度")
        private String dimension;

        @Schema(description = "维度值")
        private String dimensionValue;

        @Schema(description = "浏览门店数")
        private Long browseStoreCount;

        @Schema(description = "选择技师数")
        private Long selectTechCount;

        @Schema(description = "选择时间数")
        private Long selectTimeCount;

        @Schema(description = "确认预约数")
        private Long confirmCount;

        @Schema(description = "到店数")
        private Long arriveCount;

        @Schema(description = "完成数")
        private Long completeCount;

        @Schema(description = "整体转化率(百分比)")
        private Double overallConversion;
    }
}
