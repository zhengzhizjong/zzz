package com.zhongjitang.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("integration_lead")
public class IntegrationLeadDO extends BaseDO {

    private String leadNo;

    private String platform;

    private String platformLeadId;

    private String sourceChannel;

    private String campaignId;

    private String referrerName;

    private String customerName;

    private String customerPhone;

    private Integer customerGender;

    private Integer customerAge;

    private String customerIntent;

    private String consultationContent;

    private String aiSummary;

    private Integer leadClass;

    private Integer followStatus;

    private Long assignedTo;

    private LocalDateTime assignedAt;

    private Long convertedOrderId;

    private LocalDateTime convertedAt;
}
