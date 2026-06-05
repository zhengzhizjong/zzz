package com.zhongjitang.ai.controller;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import com.zhongjitang.ai.service.LlmGatewayService;
import com.zhongjitang.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/gateway")
@Tag(name = "AI网关")
@RequiredArgsConstructor
public class AiGatewayController {

    private final LlmGatewayService llmGatewayService;

    @PostMapping("/chat")
    @Operation(summary = "LLM对话")
    public R<LlmResponse> chat(@Valid @RequestBody LlmRequest request) {
        return R.ok(llmGatewayService.chat(request));
    }

    @GetMapping("/providers")
    @Operation(summary = "可用模型列表")
    public R<List<Map<String, Object>>> getProviders() {
        return R.ok(llmGatewayService.getAvailableProviders());
    }

    @GetMapping("/usage")
    @Operation(summary = "调用量统计")
    public R<Map<String, Object>> getUsage(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return R.ok(llmGatewayService.getUsageStatistics(startDate, endDate));
    }
}
