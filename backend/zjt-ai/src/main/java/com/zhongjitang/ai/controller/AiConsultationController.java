package com.zhongjitang.ai.controller;

import com.zhongjitang.ai.domain.vo.ConsultationResponse;
import com.zhongjitang.ai.service.AiConsultationService;
import com.zhongjitang.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/consultation")
@Tag(name = "AI问诊")
@RequiredArgsConstructor
public class AiConsultationController {

    private final AiConsultationService aiConsultationService;

    @PostMapping("/ask")
    @Operation(summary = "提问")
    public R<ConsultationResponse> ask(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        Long memberId = body.get("memberId") != null ? Long.parseLong(body.get("memberId")) : null;
        return R.ok(aiConsultationService.consult(question, memberId));
    }

    @PostMapping("/constitution")
    @Operation(summary = "体质辨识")
    public R<ConsultationResponse> getConstitution(@RequestBody Map<String, String> body) {
        String symptoms = body.get("symptoms");
        return R.ok(aiConsultationService.getConstitutionType(symptoms));
    }
}
