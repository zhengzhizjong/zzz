package com.zhongjitang.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "客服对话响应")
public class ChatResponse {

    @Schema(description = "AI回答")
    private String answer;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "推荐问题")
    private List<String> suggestedQuestions;
}
