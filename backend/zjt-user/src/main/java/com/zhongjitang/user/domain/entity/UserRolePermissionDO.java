package com.zhongjitang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_role_permission")
public class UserRolePermissionDO extends BaseDO {

    private Long roleId;

    private Long permissionId;

    @TableField(exist = false)
    private Long storeId;
}
