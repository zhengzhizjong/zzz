package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "微信登录请求")
public class WechatLoginRequest {

    @Schema(description = "微信授权code", required = true)
    @NotBlank(message = "微信授权code不能为空")
    private String code;

    @Schema(description = "加密数据")
    private String encryptedData;

    @Schema(description = "初始向量")
    private String iv;
}
