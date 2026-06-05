package com.zhongjitang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_department")
public class UserDepartmentDO extends BaseDO {

    private String deptName;

    private Long parentId;

    private Integer sortOrder;

    private Integer status;
}
