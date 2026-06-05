package com.zhongjitang.ai.llm;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import reactor.core.publisher.Mono;

public interface LlmProvider {

    Mono<LlmResponse> chat(LlmRequest request);

    String getProviderName();

    String getDefaultModel();

    boolean supports(String model);
}
