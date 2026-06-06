package com.zhongjitang.store.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store_technician_skill")
public class StoreTechnicianSkillDO extends BaseDO {

    private Long technicianId;

    private Long serviceItemId;

    /** 熟练度: 1入门 2熟练 3精通 4专家 */
    private Integer proficiency;

    @TableField(exist = false)
    private LocalDateTime certifiedAt;

    @TableField(exist = false)
    private String certificationNo;
}
