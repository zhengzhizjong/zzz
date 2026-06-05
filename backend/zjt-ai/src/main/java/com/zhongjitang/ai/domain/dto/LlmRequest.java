package com.zhongjitang.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Schema(description = "LLM请求")
public class LlmRequest {

    @NotEmpty(message = "消息列表不能为空")
    @Valid
    @Schema(description = "消息列表")
    private List<Message> messages;

    @Schema(description = "模型名称，不指定则使用默认模型")
    private String model;

    @Schema(description = "场景类型")
    private String sceneType;

    @Schema(description = "温度参数")
    private Double temperature;

    @Schema(description = "最大Token数")
    private Integer maxTokens;

    @Data
    @Schema(description = "消息")
    public static class Message {

        @NotEmpty(message = "角色不能为空")
        @Schema(description = "角色: system/user/assistant")
        private String role;

        @NotEmpty(message = "内容不能为空")
        @Schema(description = "消息内容")
        private String content;
    }
}
