package com.zhongjitang.trade.domain.entity;

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

    private Long paymentId;

    private Long memberId;

    private BigDecimal refundAmount;

    private String reason;

    /** 状态: 1待审核 2已通过 3已拒绝 4已退款 5退款失败 */
    private Integer status;

    private String approvedBy;

    private LocalDateTime approvedAt;

    private LocalDateTime refundedAt;

    private String transactionId;

    private String rejectReason;
}
