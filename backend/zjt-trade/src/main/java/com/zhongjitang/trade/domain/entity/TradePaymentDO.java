package com.zhongjitang.trade.domain.entity;

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

    private Long memberId;

    private BigDecimal amount;

    /** 支付渠道: wechat/alipay/cash/card */
    private String channel;

    /** 状态: 1待支付 2已支付 3失败 4已退款 */
    private Integer status;

    private String transactionId;

    private LocalDateTime paidAt;

    private LocalDateTime expireAt;

    private String prepayId;

    private String notifyData;
}
