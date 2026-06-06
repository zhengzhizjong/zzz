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
@TableName("trade_payment")
public class TradePaymentDO extends BaseDO {

    private String paymentNo;

    private Long orderId;

    @TableField(exist = false)
    private Long memberId;

    @TableField("payment_method")
    private String channel;

    private BigDecimal amount;

    private String transactionId;

    @TableField("payment_status")
    private Integer status;

    @TableField("payment_time")
    private LocalDateTime paidAt;

    @TableField(exist = false)
    private LocalDateTime expireAt;

    @TableField(exist = false)
    private String prepayId;

    @TableField("callback_data")
    private String notifyData;
}
