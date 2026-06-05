package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "更新会员个人信息请求")
public class UpdateProfileRequest {

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "性别: 0=未知, 1=男, 2=女")
    private Integer gender;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "头像URL")
    private String avatarUrl;
}
