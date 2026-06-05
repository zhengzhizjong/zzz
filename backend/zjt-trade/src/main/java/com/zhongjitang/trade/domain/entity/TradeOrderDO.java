package com.zhongjitang.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_order")
public class TradeOrderDO extends BaseDO {

    private String orderNo;

    private Integer orderType;

    private Long memberId;

    private String memberName;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal paidAmount;

    private BigDecimal pointsDeductAmount;

    private Integer paymentMethod;

    private Integer paymentStatus;

    private LocalDateTime paymentTime;

    private String transactionId;

    private Long appointmentId;

    private Long technicianId;

    private String sourceChannel;

    private String remark;
}
