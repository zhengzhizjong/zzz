package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "管理端更新会员请求")
public class MemberAdminUpdateRequest {

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别: 0=未知, 1=男, 2=女")
    private Integer gender;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "会员类型: 1=散客, 2=会员, 3=VIP")
    private Integer memberType;

    @Schema(description = "状态: 0=禁用, 1=正常")
    private Integer status;
}
