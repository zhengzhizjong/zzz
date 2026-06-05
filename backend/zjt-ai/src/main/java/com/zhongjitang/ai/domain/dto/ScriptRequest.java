package com.zhongjitang.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "话术生成请求")
public class ScriptRequest {

    @NotBlank(message = "场景类型不能为空")
    @Schema(description = "场景类型: consultation/upsell/followup")
    private String sceneType;

    @Schema(description = "客户信息")
    private String customerInfo;

    @Schema(description = "目标服务")
    private String targetService;

    @Schema(description = "上下文")
    private String context;
}
