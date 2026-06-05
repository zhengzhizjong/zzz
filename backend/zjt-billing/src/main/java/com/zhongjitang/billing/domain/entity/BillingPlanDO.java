package com.zhongjitang.billing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("billing_plan")
public class BillingPlanDO extends BaseDO {

    private String planCode;

    private String planName;

    private BigDecimal monthlyPrice;

    private BigDecimal yearlyPrice;

    private Integer maxStores;

    private String featuresJson;

    private String description;

    private Integer sortOrder;

    private Integer status;
}
