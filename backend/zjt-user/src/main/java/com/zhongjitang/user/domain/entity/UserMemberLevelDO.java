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

    private BigDecimal minSpent;

    private Integer minVisits;

    private BigDecimal discountRate;

    private String iconUrl;

    private String color;

    private Integer sortOrder;

    private Integer status;

    @TableField(exist = false)
    private Long storeId;
}
