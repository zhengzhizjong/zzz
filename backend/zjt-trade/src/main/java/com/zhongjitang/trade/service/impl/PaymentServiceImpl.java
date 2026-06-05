package com.zhongjitang.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.entity.TradeOrderDO;
import com.zhongjitang.trade.domain.entity.TradePaymentDO;
import com.zhongjitang.trade.domain.vo.PaymentVO;
import com.zhongjitang.trade.mapper.TradeOrderMapper;
import com.zhongjitang.trade.mapper.TradePaymentMapper;
import com.zhongjitang.trade.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final TradePaymentMapper tradePaymentMapper;
    private final TradeOrderMapper tradeOrderMapper;

    private static final AtomicInteger PAYMENT_SEQUENCE = new AtomicInteger(1);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<PaymentVO> createPayment(Long orderId, String channel) {
        // 1. 查询订单
        TradeOrderDO order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在，ID: " + orderId);
        }

        // 2. 校验订单状态：只有待支付(1)的订单可以创建支付
        if (order.getPaymentStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前订单状态不允许支付");
        }

        // 3. 检查是否已有待支付的支付单
        LambdaQueryWrapper<TradePaymentDO> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(TradePaymentDO::getOrderId, orderId)
                .eq(TradePaymentDO::getStatus, 1);
        TradePaymentDO existPayment = tradePaymentMapper.selectOne(existWrapper);
        if (existPayment != null) {
            // 已有待支付单，直接返回
            return R.ok(convertToVO(existPayment));
        }

        // 4. 生成支付编号: PAY + yyyyMMdd + 6位序号
        String paymentNo = generatePaymentNo();

        // 5. 创建支付单
        TradePaymentDO payment = new TradePaymentDO();
        payment.setPaymentNo(paymentNo);
        payment.setOrderId(orderId);
        payment.setMemberId(order.getMemberId());
        payment.setAmount(order.getTotalAmount());
        payment.setChannel(channel);
        payment.setStatus(1); // 待支付
        payment.setExpireAt(LocalDateTime.now().plusMinutes(30)); // 30分钟过期
        payment.setStoreId(order.getStoreId());

        // 6. 微信支付：调用微信JSAPI下单接口（预留）
        if ("wechat".equals(channel)) {
            // TODO: 调用微信SDK下单，获取prepayId
            payment.setPrepayId("mock_prepay_id");
        }

        tradePaymentMapper.insert(payment);

        return R.ok(convertToVO(payment));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> handleWechatNotify(String notifyData) {
        // 1. 解析微信回调数据（预留，实际使用微信SDK解析）
        log.info("收到微信支付回调: {}", notifyData);

        // TODO: 使用微信SDK验签并解析notifyData
        // 模拟解析出orderId和transactionId
        String mockOrderId = "1";
        String mockTransactionId = "WX_" + System.currentTimeMillis();

        // 2. 查询支付单
        LambdaQueryWrapper<TradePaymentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradePaymentDO::getOrderId, Long.valueOf(mockOrderId))
                .eq(TradePaymentDO::getStatus, 1);
        TradePaymentDO payment = tradePaymentMapper.selectOne(wrapper);
        if (payment == null) {
            log.warn("微信支付回调：未找到待支付记录，orderId={}", mockOrderId);
            return R.ok();
        }

        // 3. 更新支付状态
        payment.setStatus(2); // 已支付
        payment.setTransactionId(mockTransactionId);
        payment.setPaidAt(LocalDateTime.now());
        payment.setNotifyData(notifyData);
        tradePaymentMapper.updateById(payment);

        // 4. 更新订单状态
        TradeOrderDO order = tradeOrderMapper.selectById(payment.getOrderId());
        if (order != null) {
            order.setPaymentStatus(2); // 已支付
            order.setPaymentTime(LocalDateTime.now());
            order.setTransactionId(mockTransactionId);
            order.setPaymentMethod(getPaymentMethod(payment.getChannel()));
            tradeOrderMapper.updateById(order);
        }

        return R.ok();
    }

    @Override
    public R<PaymentVO> getByOrderId(Long orderId) {
        LambdaQueryWrapper<TradePaymentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradePaymentDO::getOrderId, orderId)
                .orderByDesc(TradePaymentDO::getCreatedAt)
                .last("LIMIT 1");
        TradePaymentDO payment = tradePaymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到该订单的支付记录");
        }
        return R.ok(convertToVO(payment));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> refund(Long paymentId, BigDecimal amount, String reason) {
        TradePaymentDO payment = tradePaymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付记录不存在，ID: " + paymentId);
        }

        // 只有已支付(2)的支付单可以退款
        if (payment.getStatus() != 2) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前支付状态不允许退款");
        }

        // 校验退款金额
        if (amount.compareTo(payment.getAmount()) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "退款金额不能大于支付金额");
        }

        // 调用微信退款接口（预留）
        if ("wechat".equals(payment.getChannel())) {
            // TODO: 调用微信SDK退款接口
            log.info("调用微信退款接口，paymentId={}, amount={}, reason={}", paymentId, amount, reason);
        }

        // 更新支付状态
        payment.setStatus(4); // 已退款
        tradePaymentMapper.updateById(payment);

        // 更新订单状态
        TradeOrderDO order = tradeOrderMapper.selectById(payment.getOrderId());
        if (order != null) {
            order.setPaymentStatus(6); // 已退款
            tradeOrderMapper.updateById(order);
        }

        return R.ok();
    }

    private PaymentVO convertToVO(TradePaymentDO payment) {
        PaymentVO vo = new PaymentVO();
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setOrderId(payment.getOrderId());
        vo.setAmount(payment.getAmount());
        vo.setChannel(payment.getChannel());
        vo.setStatus(payment.getStatus());
        vo.setTransactionId(payment.getTransactionId());
        vo.setPaidAt(payment.getPaidAt());
        vo.setWechatPrepayId(payment.getPrepayId());
        return vo;
    }

    private Integer getPaymentMethod(String channel) {
        switch (channel) {
            case "wechat":
                return 1;
            case "alipay":
                return 2;
            case "cash":
                return 3;
            case "card":
                return 4;
            default:
                return 0;
        }
    }

    private String generatePaymentNo() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        int seq = PAYMENT_SEQUENCE.getAndIncrement();
        if (seq > 999999) {
            PAYMENT_SEQUENCE.set(1);
            seq = 1;
        }
        return "PAY" + datePart + String.format("%06d", seq);
    }
}
