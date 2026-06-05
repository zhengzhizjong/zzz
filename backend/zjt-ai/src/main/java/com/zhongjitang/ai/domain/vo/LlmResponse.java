package com.zhongjitang.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "LLM响应")
public class LlmResponse {

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "使用的模型")
    private String model;

    @Schema(description = "模型提供方")
    private String provider;

    @Schema(description = "提示Token数")
    private Integer promptTokens;

    @Schema(description = "完成Token数")
    private Integer completionTokens;

    @Schema(description = "总Token数")
    private Integer totalTokens;

    @Schema(description = "延迟毫秒数")
    private Long latencyMs;
}
