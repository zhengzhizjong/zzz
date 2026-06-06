package com.zhongjitang.integration.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新集成配置请求")
public class ConfigUpdateRequest {

    @Schema(description = "配置名称")
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
