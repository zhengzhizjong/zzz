package com.zhongjitang.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.entity.TradeAppointmentDO;
import com.zhongjitang.trade.domain.entity.TradeOrderDO;
import com.zhongjitang.trade.domain.entity.TradeOrderItemDO;
import com.zhongjitang.trade.mapper.TradeAppointmentMapper;
import com.zhongjitang.trade.mapper.TradeOrderItemMapper;
import com.zhongjitang.trade.mapper.TradeOrderMapper;
import com.zhongjitang.trade.service.impl.TradeOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeOrderServiceImplTest {

    @Mock
    private TradeOrderMapper tradeOrderMapper;

    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;

    @Mock
    private TradeAppointmentMapper tradeAppointmentMapper;

    @InjectMocks
    private TradeOrderServiceImpl tradeOrderService;

    private TradeAppointmentDO appointment;
    private TradeOrderDO order;

    @BeforeEach
    void setUp() {
        appointment = new TradeAppointmentDO();
        appointment.setId(1L);
        appointment.setAppointmentNo("APT20260605000001");
        appointment.setMemberId(100L);
        appointment.setMemberName("张三");
        appointment.setServiceItemId(1L);
        appointment.setServiceItemName("中医推拿");
        appointment.setTechnicianId(1L);
        appointment.setTechnicianName("李技师");
        appointment.setAppointmentDate(LocalDate.of(2026, 6, 5));
        appointment.setAppointmentTime(LocalTime.of(9, 0));
        appointment.setTimeSlot("09:00");
        appointment.setDurationMinutes(60);
        appointment.setStoreId(1L);

        order = new TradeOrderDO();
        order.setId(1L);
        order.setOrderNo("ORD20260605000001");
        order.setMemberId(100L);
        order.setMemberName("张三");
        order.setTotalAmount(new BigDecimal("299.00"));
        order.setPaymentStatus(1);
        order.setAppointmentId(1L);
        order.setStoreId(1L);
    }

    @Test
    void testCreateFromAppointmentSuccess() {
        when(tradeAppointmentMapper.selectById(1L)).thenReturn(appointment);
        when(tradeOrderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeOrderMapper.insert(any(TradeOrderDO.class))).thenReturn(1);
        when(tradeOrderItemMapper.insert(any(TradeOrderItemDO.class))).thenReturn(1);

        R<TradeOrderDO> result = tradeOrderService.createFromAppointment(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getOrderNo().startsWith("ORD"));
        assertEquals(1, result.getData().getOrderType());
        assertEquals(100L, result.getData().getMemberId());

        verify(tradeOrderMapper, times(1)).insert(any(TradeOrderDO.class));
        verify(tradeOrderItemMapper, times(1)).insert(any(TradeOrderItemDO.class));
    }

    @Test
    void testCreateFromAppointmentNotFound() {
        when(tradeAppointmentMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tradeOrderService.createFromAppointment(999L);
        });
        assertEquals(ErrorCode.APPOINTMENT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testCreateFromAppointmentAlreadyExists() {
        when(tradeAppointmentMapper.selectById(1L)).thenReturn(appointment);
        when(tradeOrderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tradeOrderService.createFromAppointment(1L);
        });
        assertEquals(ErrorCode.CONFLICT.getCode(), exception.getCode());
    }

    @Test
    void testCompleteSuccess() {
        order.setPaymentStatus(2); // 已支付
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(tradeOrderMapper.updateById(any(TradeOrderDO.class))).thenReturn(1);

        R<Void> result = tradeOrderService.complete(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(tradeOrderMapper, times(1)).updateById(any(TradeOrderDO.class));
    }

    @Test
    void testCompleteOrderNotFound() {
        when(tradeOrderMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tradeOrderService.complete(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testCompleteInvalidStatus() {
        order.setPaymentStatus(1); // 待支付
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tradeOrderService.complete(1L);
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testCancelSuccess() {
        order.setPaymentStatus(1); // 待支付
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        when(tradeOrderMapper.updateById(any(TradeOrderDO.class))).thenReturn(1);

        R<Void> result = tradeOrderService.cancel(1L, "不想要了");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(tradeOrderMapper, times(1)).updateById(any(TradeOrderDO.class));
    }

    @Test
    void testCancelAlreadyPaid() {
        order.setPaymentStatus(2); // 已支付
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tradeOrderService.cancel(1L, "不想要了");
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testGetByIdSuccess() {
        when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        R<TradeOrderDO> result = tradeOrderService.getById(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("ORD20260605000001", result.getData().getOrderNo());
    }

    @Test
    void testGetByIdNotFound() {
        when(tradeOrderMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tradeOrderService.getById(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testPageSuccess() {
        Page<TradeOrderDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Collections.singletonList(order));
        pageResult.setTotal(1);
        when(tradeOrderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        var result = tradeOrderService.page(1, 20, null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
    }
}
