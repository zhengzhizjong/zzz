package com.zhongjitang.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_audit_log")
public class SysAuditLogDO extends BaseDO {

    @TableField(exist = false)
    private Long storeId;

    private Long userId;

    private String userName;

    private String operation;

    private String method;

    private String module;

    private String targetId;

    private String targetType;

    private String oldValue;

    private String newValue;

    private String ip;

    private String userAgent;

    private Long duration;

    private Integer status;
}
