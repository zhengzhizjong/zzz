package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@Schema(description = "创建员工请求")
public class EmployeeCreateRequest {

    @Schema(description = "员工姓名", required = true)
    @NotBlank(message = "员工姓名不能为空")
    private String name;

    @Schema(description = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "性别: 0=未知, 1=男, 2=女")
    private Integer gender;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "入职日期")
    private LocalDate hireDate;
}
