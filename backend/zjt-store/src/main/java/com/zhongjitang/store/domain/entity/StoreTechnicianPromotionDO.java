package com.zhongjitang.store.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store_technician_promotion")
public class StoreTechnicianPromotionDO extends BaseDO {

    private Long technicianId;

    private String promoCode;

    private String shareLink;

    private Integer clickCount;

    private Integer registerCount;

    private Integer visitCount;

    private Integer orderCount;

    private BigDecimal commissionEarned;

    private BigDecimal commissionPending;

    private Integer status;
}
