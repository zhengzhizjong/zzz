package com.zhongjitang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_health_profile")
public class UserHealthProfileDO extends BaseDO {

    private Long memberId;

    private String constitutionType;

    private String constitutionName;

    private String allergies;

    private String contraindications;

    private String medicalHistory;

    private String medication;

    private String notes;

    private String lastUpdatedBy;
}
