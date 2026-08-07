package com.zhongjitang.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "刷新Token")
    private String refreshToken;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "职位: store_manager店长, receptionist前台, therapist理疗师")
    private String position;

    @Schema(description = "员工编号")
    private String employeeNo;

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "技师ID（仅理疗师角色有值）")
    private Long technicianId;
}
