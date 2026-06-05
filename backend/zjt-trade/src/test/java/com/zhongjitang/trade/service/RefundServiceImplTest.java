package com.zhongjitang.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.RefundApplyRequest;
import com.zhongjitang.trade.domain.entity.TradeOrderDO;
import com.zhongjitang.trade.domain.entity.TradePaymentDO;
import com.zhongjitang.trade.domain.entity.TradeRefundDO;
import com.zhongjitang.trade.mapper.TradeOrderMapper;
import com.zhongjitang.trade.mapper.TradePaymentMapper;
import com.zhongjitang.trade.mapper.TradeRefundMapper;
import com.zhongjitang.trade.service.impl.RefundServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceImplTest {

    @Mock
    private TradeRefundMapper tradeRefundMapper;

    @Mock
    private TradeOrderMapper tradeOrderMapper;

    @Mock
    private TradePaymentMapper tradePaymentMapper;

    @Mock
    private IPaymentService paymentService;

    @InjectMocks
    private RefundServiceImpl refundService;

    private TradeOrderDO order;
    private RefundApplyRequest applyRequest;
    private TradeRefundDO refund;

    @BeforeEach
    void setUp() {
        order = new TradeOrderDO();
        order.setId(1L);
        order.setOrderNo("ORD20260605000001");
        order.setMemberId(100L);
        order.setTotalAmount(new BigDecimal("299.00"));
        order.setPaidAmount(new BigDecimal("299.00"));
        order.setPaymentStatus(2); // 已支付
        order.setStoreId(1L);

        applyRequest = new RefundApplyRequest();
        applyRequest.setOrderId(1L);
        applyRequest.setRefundAmount(new BigDecimal("299.00"));
        applyRequest.setReason("服务不满意");

        refund = new TradeRefundDO();
        refund.setId(1L);
        refund.setRefundNo("REF20260605000001");
        refund.setOrderId(1L);
        refund.setPaymentId(1L);
        refund.setMemberId(100L);
        refund.setRefundAmount(new BigDecimal("299.00"));
        refund.setReason("服务不满意");
        refund.setStatus(1); // 待审核
        refund.setStoreId(1L);
    }

    @Test
    void testApplySuccess() {
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(tradePaymentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new TradePaymentDO());
        when(tradeRefundMapper.insert(any(TradeRefundDO.class))).thenReturn(1);

        R<TradeRefundDO> result = refundService.apply(applyRequest);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getRefundNo().startsWith("REF"));
        assertEquals(1, result.getData().getStatus());

        verify(tradeRefundMapper, times(1)).insert(any(TradeRefundDO.class));
    }

    @Test
    void testApplyOrderNotFound() {
        applyRequest.setOrderId(999L);
        when(tradeOrderMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundService.apply(applyRequest);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testApplyInvalidOrderStatus() {
        order.setPaymentStatus(1); // 待支付
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundService.apply(applyRequest);
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testApplyAmountExceed() {
        applyRequest.setRefundAmount(new BigDecimal("500.00"));
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundService.apply(applyRequest);
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testApproveSuccess() {
        when(tradeRefundMapper.selectById(1L)).thenReturn(refund);
        when(tradeRefundMapper.updateById(any(TradeRefundDO.class))).thenReturn(1);
        when(paymentService.refund(anyLong(), any(BigDecimal.class), anyString())).thenReturn(R.ok());

        R<Void> result = refundService.approve(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(tradeRefundMapper, atLeastOnce()).updateById(any(TradeRefundDO.class));
        verify(paymentService, times(1)).refund(anyLong(), any(BigDecimal.class), anyString());
    }

    @Test
    void testApproveNotFound() {
        when(tradeRefundMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundService.approve(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testApproveInvalidStatus() {
        refund.setStatus(2); // 已通过
        when(tradeRefundMapper.selectById(1L)).thenReturn(refund);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundService.approve(1L);
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testRejectSuccess() {
        when(tradeRefundMapper.selectById(1L)).thenReturn(refund);
        when(tradeRefundMapper.updateById(any(TradeRefundDO.class))).thenReturn(1);

        R<Void> result = refundService.reject(1L, "不符合退款条件");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(tradeRefundMapper, times(1)).updateById(any(TradeRefundDO.class));
    }

    @Test
    void testRejectInvalidStatus() {
        refund.setStatus(3); // 已拒绝
        when(tradeRefundMapper.selectById(1L)).thenReturn(refund);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundService.reject(1L, "不符合退款条件");
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testGetByIdSuccess() {
        when(tradeRefundMapper.selectById(1L)).thenReturn(refund);

        R<TradeRefundDO> result = refundService.getById(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("REF20260605000001", result.getData().getRefundNo());
    }

    @Test
    void testGetByIdNotFound() {
        when(tradeRefundMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            refundService.getById(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }
}
