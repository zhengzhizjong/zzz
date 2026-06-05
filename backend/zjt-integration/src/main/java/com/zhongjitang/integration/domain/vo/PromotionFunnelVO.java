package com.zhongjitang.integration.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "推广漏斗VO")
public class PromotionFunnelVO {

    @Schema(description = "统计周期")
    private Period period;

    @Schema(description = "漏斗各步骤数据")
    private Map<String, FunnelStep> funnel;

    @Schema(description = "各步骤间转化率")
    private Map<String, Double> conversion;

    @Schema(description = "按渠道统计")
    private List<ChannelData> byChannel;

    @Data
    @Schema(description = "统计周期")
    public static class Period {

        @Schema(description = "开始日期")
        private LocalDate startDate;

        @Schema(description = "结束日期")
        private LocalDate endDate;
    }

    @Data
    @Schema(description = "漏斗步骤")
    public static class FunnelStep {

        @Schema(description = "数量")
        private Long count;

        @Schema(description = "转化率(百分比)")
        private Double rate;
    }

    @Data
    @Schema(description = "渠道数据")
    public static class ChannelData {

        @Schema(description = "渠道")
        private String channel;

        @Schema(description = "点击数")
        private Long click;

        @Schema(description = "注册数")
        private Long register;

        @Schema(description = "首次访问数")
        private Long firstVisit;

        @Schema(description = "首单数")
        private Long firstOrder;

        @Schema(description = "复购数")
        private Long repurchase;
    }
}
