package com.zhongjitang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_permission")
public class UserPermissionDO extends BaseDO {

    private String permissionName;

    private String permissionCode;

    @TableField("permission_type")
    private String resourceType;

    @TableField(exist = false)
    private String resourceId;

    private Long parentId;

    private Integer sortOrder;

    private Integer status;
}
