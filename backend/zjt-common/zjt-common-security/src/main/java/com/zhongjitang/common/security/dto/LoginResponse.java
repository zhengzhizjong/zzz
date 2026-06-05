package com.zhongjitang.common.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录响应DTO
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse {

    /** JWT Token */
    @Schema(description = "JWT Token")
    private String token;

    /** 用户ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 用户名 */
    @Schema(description = "用户名")
    private String username;

    /** 用户类型 */
    @Schema(description = "用户类型: member/employee/admin")
    private String userType;

    /** 租户ID */
    @Schema(description = "租户ID")
    private Long tenantId;

    /** 门店ID */
    @Schema(description = "门店ID")
    private Long storeId;

    /** Token过期时间 */
    @Schema(description = "Token过期时间")
    private LocalDateTime expireAt;
}
