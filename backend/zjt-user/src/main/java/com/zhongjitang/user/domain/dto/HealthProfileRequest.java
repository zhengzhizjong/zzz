package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "健康档案请求")
public class HealthProfileRequest {

    @Schema(description = "会员ID", required = true)
    private Long memberId;

    @Schema(description = "体质类型")
    private String constitutionType;

    @Schema(description = "体质名称")
    private String constitutionName;

    @Schema(description = "过敏史")
    private String allergies;

    @Schema(description = "禁忌症")
    private String contraindications;

    @Schema(description = "病史")
    private String medicalHistory;

    @Schema(description = "用药情况")
    private String medication;

    @Schema(description = "备注")
    private String notes;
}
