package com.zhongjitang.store.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "技师业绩VO")
public class TechnicianPerformanceVO {

    @Schema(description = "本月营收")
    private BigDecimal monthRevenue;

    @Schema(description = "本月目标")
    private BigDecimal monthTarget;

    @Schema(description = "目标完成率(百分比)")
    private Double targetCompletion;

    @Schema(description = "本月服务次数")
    private Integer monthServiceCount;

    @Schema(description = "客单价")
    private BigDecimal avgPrice;

    @Schema(description = "本月评分")
    private BigDecimal monthRating;

    @Schema(description = "排名")
    private Integer rank;
}
