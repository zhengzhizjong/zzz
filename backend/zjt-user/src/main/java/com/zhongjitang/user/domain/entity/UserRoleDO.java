package com.zhongjitang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_role")
public class UserRoleDO extends BaseDO {

    private String roleName;

    private String roleCode;

    private String description;

    private Integer status;
}
