package com.zhongjitang.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_refund")
public class TradeRefundDO extends BaseDO {

    private String refundNo;

    private Long orderId;

    @TableField(exist = false)
    private Long paymentId;

    @TableField(exist = false)
    private Long memberId;

    private BigDecimal refundAmount;

    @TableField("refund_reason")
    private String reason;

    @TableField("refund_status")
    private Integer status;

    @TableField(exist = false)
    private String approvedBy;

    @TableField(exist = false)
    private LocalDateTime approvedAt;

    @TableField("refund_time")
    private LocalDateTime refundedAt;

    @TableField(exist = false)
    private String transactionId;

    @TableField(exist = false)
    private String rejectReason;
}
