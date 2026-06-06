package com.zhongjitang.billing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("billing_usage")
public class BillingUsageDO extends BaseDO {

    private String usageType;

    private LocalDate usageDate;

    private Integer usageCount;

    private BigDecimal unitPrice;

    private Integer costCents;

    private String detailJson;
}
