package com.zhongjitang.store.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store_technician")
public class StoreTechnicianDO extends BaseDO {

    private Long employeeId;

    private String technicianNo;

    private Integer skillLevel;

    private String skilledItems;

    private String defaultSchedule;

    private Integer monthServiceCount;

    private BigDecimal monthRevenue;

    private BigDecimal monthRating;

    private Integer totalServiceCount;

    private Integer status;

    private Integer isOnline;

    /** 技师姓名（关联user_employee表） */
    @TableField(exist = false)
    private String name;

    /** 技师头像（关联user_employee表） */
    @TableField(exist = false)
    private String avatarUrl;

    /** 门店名称（关联store_info表） */
    @TableField(exist = false)
    private String storeName;
}
