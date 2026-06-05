package com.zhongjitang.trade.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "支付信息VO")
public class PaymentVO {

    @Schema(description = "支付编号", example = "PAY20260605000001")
    private String paymentNo;

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "支付金额", example = "299.00")
    private BigDecimal amount;

    @Schema(description = "支付渠道: wechat/alipay/cash/card", example = "wechat")
    private String channel;

    @Schema(description = "支付状态: 1待支付 2已支付 3失败 4已退款", example = "1")
    private Integer status;

    @Schema(description = "第三方交易号")
    private String transactionId;

    @Schema(description = "支付时间")
    private LocalDateTime paidAt;

    @Schema(description = "微信预支付ID")
    private String wechatPrepayId;

    @Schema(description = "微信支付签名(小程序调起支付用)")
    private String wechatPaySign;
}
