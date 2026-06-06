package com.zhongjitang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_member_level")
public class UserMemberLevelDO extends BaseDO {

    private String levelName;

    private String levelCode;

    @TableField("min_points")
    private Integer minPoints;

    @TableField(exist = false)
    private Integer minVisits;

    @TableField("max_points")
    private Integer maxPoints;

    private BigDecimal discountRate;

    @TableField("benefits_json")
    private String benefitsJson;

    @TableField(exist = false)
    private String iconUrl;

    @TableField(exist = false)
    private String color;

    private Integer sortOrder;

    @TableField(exist = false)
    private Integer status;
}
