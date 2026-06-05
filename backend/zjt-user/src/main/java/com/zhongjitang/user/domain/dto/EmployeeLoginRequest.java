package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Schema(description = "员工登录请求")
public class EmployeeLoginRequest {

    @Schema(description = "手机号", example = "13800138000", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}
