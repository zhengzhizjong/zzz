package com.zhongjitang.ai.controller;

import com.zhongjitang.ai.domain.dto.ScriptRequest;
import com.zhongjitang.ai.domain.vo.ScriptResponse;
import com.zhongjitang.ai.service.AiScriptService;
import com.zhongjitang.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/ai/script")
@Tag(name = "AI话术")
@RequiredArgsConstructor
public class AiScriptController {

    private final AiScriptService aiScriptService;

    @PostMapping("/generate")
    @Operation(summary = "生成话术")
    public R<ScriptResponse> generate(@Valid @RequestBody ScriptRequest request) {
        return R.ok(aiScriptService.generateScript(request));
    }
}
