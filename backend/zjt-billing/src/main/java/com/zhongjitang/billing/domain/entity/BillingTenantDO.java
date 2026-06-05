package com.zhongjitang.billing.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("billing_tenant")
public class BillingTenantDO extends BaseDO {

    private String tenantNo;

    private String tenantName;

    private String planCode;

    private LocalDate planExpireDate;

    private Integer whiteLabelEnabled;

    private String whiteLabelConfig;

    private String brandName;

    private String brandLogo;

    private String brandTheme;

    private Long adminUserId;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private Integer status;

    private LocalDate trialExpireDate;
}
