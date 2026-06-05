package com.zhongjitang.billing.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "用量汇总VO")
public class UsageSummaryVO {

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "月份", example = "2026-06")
    private String month;

    @Schema(description = "用量指标: metricType -> totalValue")
    private Map<String, Integer> metrics;

    @Schema(description = "配额: metricType -> planQuota")
    private Map<String, Integer> quotas;

    @Schema(description = "超额: metricType -> overQuota")
    private Map<String, Integer> overages;
}
