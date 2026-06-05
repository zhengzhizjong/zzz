package com.zhongjitang.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "问诊响应")
public class ConsultationResponse {

    @Schema(description = "AI回答")
    private String answer;

    @Schema(description = "体质类型编码")
    private String constitutionType;

    @Schema(description = "体质类型名称")
    private String constitutionName;

    @Schema(description = "养生建议")
    private List<String> suggestions;

    @Schema(description = "推荐服务项目")
    private List<String> recommendedItems;
}
