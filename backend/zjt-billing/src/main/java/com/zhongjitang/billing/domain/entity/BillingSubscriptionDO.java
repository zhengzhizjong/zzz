package com.zhongjitang.billing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("billing_subscription")
public class BillingSubscriptionDO extends BaseDO {

    private Long tenantId;

    private String planCode;

    private String planName;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer billingCycle;

    private Integer autoRenew;

    private String paymentMethod;

    private Integer paymentStatus;

    private BigDecimal paidAmount;

    private String status;
}
