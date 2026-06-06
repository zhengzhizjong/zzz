package com.zhongjitang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_health_profile")
public class UserHealthProfileDO extends BaseDO {

    private Long memberId;

    @TableField("constitution_type")
    private String constitutionType;

    @TableField(exist = false)
    private String constitutionName;

    @TableField("health_data")
    private String healthData;

    @TableField(exist = false)
    private String allergies;

    @TableField(exist = false)
    private String contraindications;

    @TableField(exist = false)
    private String medicalHistory;

    @TableField(exist = false)
    private String medication;

    @TableField(exist = false)
    private String notes;

    @TableField("last_check_time")
    private LocalDateTime lastCheckTime;

    @TableField("is_encrypted")
    private Integer isEncrypted;

    @TableField(exist = false)
    private String lastUpdatedBy;
}
