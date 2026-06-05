package com.zhongjitang.common.mybatis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库实体基类
 * <p>
 * 包含所有业务表共有的审计字段：租户ID、门店ID、
 * 创建时间、更新时间、创建人、更新人、逻辑删除标记。
 * </p>
 */
@Data
public abstract class BaseDO {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 门店ID，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long storeId;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 创建人，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /** 更新人，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /** 逻辑删除标记：0=未删除, 1=已删除 */
    @TableLogic
    private Integer isDeleted;
}
