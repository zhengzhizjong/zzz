package com.zhongjitang.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.entity.TradeOrderDO;
import com.zhongjitang.trade.domain.entity.TradePaymentDO;
import com.zhongjitang.trade.domain.vo.PaymentVO;
import com.zhongjitang.trade.mapper.TradeOrderMapper;
import com.zhongjitang.trade.mapper.TradePaymentMapper;
import com.zhongjitang.trade.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private TradePaymentMapper tradePaymentMapper;

    @Mock
    private TradeOrderMapper tradeOrderMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private TradeOrderDO order;
    private TradePaymentDO payment;

    @BeforeEach
    void setUp() {
        order = new TradeOrderDO();
        order.setId(1L);
        order.setOrderNo("ORD20260605000001");
        order.setMemberId(100L);
        order.setTotalAmount(new BigDecimal("299.00"));
        order.setPaymentStatus(1);
        order.setStoreId(1L);

        payment = new TradePaymentDO();
        payment.setId(1L);
        payment.setPaymentNo("PAY20260605000001");
        payment.setOrderId(1L);
        payment.setMemberId(100L);
        payment.setAmount(new BigDecimal("299.00"));
        payment.setChannel("wechat");
        payment.setStatus(1);
        payment.setStoreId(1L);
    }

    @Test
    void testCreatePaymentSuccess() {
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(tradePaymentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(tradePaymentMapper.insert(any(TradePaymentDO.class))).thenReturn(1);

        R<PaymentVO> result = paymentService.createPayment(1L, "wechat");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getPaymentNo().startsWith("PAY"));
        assertEquals("wechat", result.getData().getChannel());
        assertEquals(1, result.getData().getStatus());

        verify(tradePaymentMapper, times(1)).insert(any(TradePaymentDO.class));
    }

    @Test
    void testCreatePaymentOrderNotFound() {
        when(tradeOrderMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.createPayment(999L, "wechat");
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testCreatePaymentInvalidOrderStatus() {
        order.setPaymentStatus(2); // 已支付
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.createPayment(1L, "wechat");
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testCreatePaymentExistingPending() {
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(tradePaymentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(payment);

        R<PaymentVO> result = paymentService.createPayment(1L, "wechat");

        assertNotNull(result);
        assertEquals("PAY20260605000001", result.getData().getPaymentNo());
        // 不应创建新的支付单
        verify(tradePaymentMapper, never()).insert(any(TradePaymentDO.class));
    }

    @Test
    void testGetByOrderIdSuccess() {
        when(tradePaymentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(payment);

        R<PaymentVO> result = paymentService.getByOrderId(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("PAY20260605000001", result.getData().getPaymentNo());
    }

    @Test
    void testGetByOrderIdNotFound() {
        when(tradePaymentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.getByOrderId(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testRefundSuccess() {
        payment.setStatus(2); // 已支付
        when(tradePaymentMapper.selectById(1L)).thenReturn(payment);
        when(tradePaymentMapper.updateById(any(TradePaymentDO.class))).thenReturn(1);
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(tradeOrderMapper.updateById(any(TradeOrderDO.class))).thenReturn(1);

        R<Void> result = paymentService.refund(1L, new BigDecimal("299.00"), "服务不满意");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(tradePaymentMapper, times(1)).updateById(any(TradePaymentDO.class));
        verify(tradeOrderMapper, times(1)).updateById(any(TradeOrderDO.class));
    }

    @Test
    void testRefundAmountExceed() {
        payment.setStatus(2);
        when(tradePaymentMapper.selectById(1L)).thenReturn(payment);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.refund(1L, new BigDecimal("500.00"), "服务不满意");
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testRefundInvalidStatus() {
        payment.setStatus(1); // 待支付
        when(tradePaymentMapper.selectById(1L)).thenReturn(payment);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.refund(1L, new BigDecimal("299.00"), "服务不满意");
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testHandleWechatNotifySuccess() {
        payment.setStatus(1);
        when(tradePaymentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(payment);
        when(tradePaymentMapper.updateById(any(TradePaymentDO.class))).thenReturn(1);
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(tradeOrderMapper.updateById(any(TradeOrderDO.class))).thenReturn(1);

        R<Void> result = paymentService.handleWechatNotify("{\"mock\":\"data\"}");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(tradePaymentMapper, times(1)).updateById(any(TradePaymentDO.class));
        verify(tradeOrderMapper, times(1)).updateById(any(TradeOrderDO.class));
    }
}
