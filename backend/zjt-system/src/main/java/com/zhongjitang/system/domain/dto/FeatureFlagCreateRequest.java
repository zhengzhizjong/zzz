package com.zhongjitang.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "创建功能开关请求")
public class FeatureFlagCreateRequest {

    @NotBlank(message = "开关键不能为空")
    @Schema(description = "开关键", example = "ai_recommendation")
    private String flagKey;

    @NotBlank(message = "开关名称不能为空")
    @Schema(description = "开关名称", example = "AI推荐功能")
    private String flagName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "默认值:0关 1开")
    private Integer defaultValue;

    @Schema(description = "类型:1全局 2租户 3门店 4用户 5百分比")
    private Integer type;

    @Schema(description = "百分比(0-100)")
    private Integer percentage;

    @Schema(description = "规则(JSON)")
    private String rulesJson;
}
