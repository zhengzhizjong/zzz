package com.zhongjitang.ai.controller;

import com.zhongjitang.ai.domain.vo.ChatResponse;
import com.zhongjitang.ai.service.AiChatService;
import com.zhongjitang.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/chat")
@Tag(name = "AI客服")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping
    @Operation(summary = "智能对话")
    public R<ChatResponse> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        String sessionId = body.get("sessionId");
        Long memberId = body.get("memberId") != null ? Long.parseLong(body.get("memberId")) : null;
        return R.ok(aiChatService.chat(message, sessionId, memberId));
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "清除会话")
    public R<Void> clearSession(@PathVariable String sessionId) {
        aiChatService.clearSession(sessionId);
        return R.ok();
    }
}
