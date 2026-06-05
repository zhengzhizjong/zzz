package com.zhongjitang.billing.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(description = "创建套餐请求")
public class BillingPlanCreateRequest {

    @NotBlank(message = "套餐编码不能为空")
    @Schema(description = "套餐编码", example = "basic")
    private String planCode;

    @NotBlank(message = "套餐名称不能为空")
    @Schema(description = "套餐名称", example = "基础版")
    private String planName;

    @NotNull(message = "月度价格不能为空")
    @Schema(description = "月度价格", example = "299.00")
    private BigDecimal monthlyPrice;

    @Schema(description = "年度价格", example = "2990.00")
    private BigDecimal yearlyPrice;

    @Schema(description = "最大门店数", example = "3")
    private Integer maxStores;

    @Schema(description = "功能特性(JSON)")
    private String featuresJson;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "排序", example = "0")
    private Integer sortOrder;
}
