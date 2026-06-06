package com.zhongjitang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_employee")
public class UserEmployeeDO extends BaseDO {

    private String employeeNo;

    private String name;

    private String phone;

    private Integer gender;

    @TableField(exist = false)
    private String avatarUrl;

    private Long departmentId;

    private String position;

    private Long roleId;

    private Integer status;

    private LocalDate hireDate;

    @TableField(exist = false)
    private LocalDateTime lastLoginAt;
}
