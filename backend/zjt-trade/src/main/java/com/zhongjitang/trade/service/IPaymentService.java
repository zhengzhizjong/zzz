package com.zhongjitang.trade.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.vo.PaymentVO;

import java.math.BigDecimal;

public interface IPaymentService {

    R<PaymentVO> createPayment(Long orderId, String channel);

    R<Void> handleWechatNotify(String notifyData);

    R<PaymentVO> getByOrderId(Long orderId);

    R<Void> refund(Long paymentId, BigDecimal amount, String reason);
}
