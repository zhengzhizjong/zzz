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

    private String module;

    @TableField("action")
    private String operation;

    @TableField("operator_id")
    private Long userId;

    @TableField("operator_name")
    private String userName;

    @TableField(exist = false)
    private String method;

    private String targetType;

    @TableField("target_id")
    private String targetId;

    private String oldValue;

    private String newValue;

    private String ip;

    private String userAgent;

    @TableField(exist = false)
    private Long duration;

    @TableField("result")
    private Integer status;
}
