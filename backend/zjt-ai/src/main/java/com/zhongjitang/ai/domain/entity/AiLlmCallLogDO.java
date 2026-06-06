package com.zhongjitang.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_llm_call_log")
public class AiLlmCallLogDO extends BaseDO {

    private String requestId;

    private String taskType;

    @TableField("model_provider")
    private String provider;

    @TableField("model_name")
    private String model;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Integer latencyMs;

    private Integer firstTokenMs;

    @TableField("cost_cents")
    private BigDecimal costAmount;

    private Integer isSuccess;

    @TableField("error_code")
    private Integer statusCode;

    private String errorMessage;

    private Integer fallbackTriggered;

    @TableField(exist = false)
    private String sceneType;

    @TableField(exist = false)
    private String sceneId;
}
