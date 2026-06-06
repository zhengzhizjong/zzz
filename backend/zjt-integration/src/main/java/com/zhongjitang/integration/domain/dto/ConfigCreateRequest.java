package com.zhongjitang.integration.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "创建集成配置请求")
public class ConfigCreateRequest {

    @NotBlank(message = "平台不能为空")
    @Schema(description = "平台: wecom/douyin/meituan/other", example = "wecom")
    private String platform;

    @NotBlank(message = "配置名称不能为空")
    @Schema(description = "配置名称", example = "企业微信配置")
    private String configName;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "应用密钥")
    private String appSecret;

    @Schema(description = "回调地址")
    private String callbackUrl;

    @Schema(description = "扩展配置(JSON)")
    private String extraConfig;
}
