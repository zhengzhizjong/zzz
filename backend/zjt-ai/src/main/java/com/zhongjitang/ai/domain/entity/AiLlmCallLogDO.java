package com.zhongjitang.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_llm_call_log")
public class AiLlmCallLogDO extends BaseDO {

    private String model;

    private String provider;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Integer latencyMs;

    private Integer statusCode;

    private String errorMessage;

    private String sceneType;

    private String sceneId;

    private BigDecimal costAmount;
}
