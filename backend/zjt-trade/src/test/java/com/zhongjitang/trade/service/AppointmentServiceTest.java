package com.zhongjitang.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.common.redis.util.RedisUtil;
import com.zhongjitang.trade.domain.dto.AppointmentCancelRequest;
import com.zhongjitang.trade.domain.dto.AppointmentCreateRequest;
import com.zhongjitang.trade.domain.dto.AppointmentModifyRequest;
import com.zhongjitang.trade.domain.entity.TradeAppointmentDO;
import com.zhongjitang.trade.domain.entity.TradeAppointmentLockDO;
import com.zhongjitang.trade.domain.vo.AppointmentResponse;
import com.zhongjitang.trade.domain.vo.MyAppointmentVO;
import com.zhongjitang.trade.mapper.TradeAppointmentLockMapper;
import com.zhongjitang.trade.mapper.TradeAppointmentMapper;
import com.zhongjitang.trade.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("预约服务测试")
class AppointmentServiceTest {

    @Mock
    private TradeAppointmentMapper tradeAppointmentMapper;

    @Mock
    private TradeAppointmentLockMapper tradeAppointmentLockMapper;

    @Mock
    private AppointmentLockService appointmentLockService;

    @Mock
    private AppointmentSlotService appointmentSlotService;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private AppointmentCreateRequest createRequest;
    private TradeAppointmentLockDO validLock;

    @BeforeEach
    void setUp() {
        createRequest = new AppointmentCreateRequest();
        createRequest.setStoreId(1L);
        createRequest.setDate(LocalDate.of(2026, 6, 5));
        createRequest.setTimeSlot("09:00");
        createRequest.setTechnicianId(1L);
        createRequest.setLockId("LOCK_20260605_ABCD1234");
        createRequest.setMemberId(100L);
        createRequest.setDurationMinutes(60);

        validLock = new TradeAppointmentLockDO();
        validLock.setId(1L);
        validLock.setLockId("LOCK_20260605_ABCD1234");
        validLock.setTechnicianId(1L);
        validLock.setLockDate(LocalDate.of(2026, 6, 5));
        validLock.setTimeSlot("09:00");
        validLock.setMemberId(100L);
        validLock.setStatus(1);
        validLock.setExpireAt(LocalDateTime.now().plusMinutes(3));
        validLock.setStoreId(1L);
    }

    @Test
    @DisplayName("创建预约-正常流程")
    void 创建预约成功() {
        when(tradeAppointmentLockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(validLock);
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeAppointmentMapper.insert(any(TradeAppointmentDO.class))).thenReturn(1);
        when(tradeAppointmentLockMapper.updateById(any(TradeAppointmentLockDO.class))).thenReturn(1);

        R<AppointmentResponse> result = appointmentService.create(createRequest);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getAppointmentNo().startsWith("APT"));
        assertEquals("my_appointments", result.getData().getRedirect());
        assertEquals(1, result.getData().getStatus());
        verify(redisUtil, times(2)).delete(anyString());
    }

    @Test
    @DisplayName("创建预约-锁档已过期抛异常")
    void 创建预约锁档过期() {
        when(tradeAppointmentLockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.create(createRequest);
        });

        assertEquals(ErrorCode.SLOT_TEMP_LOCKED.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("锁档已过期"));
    }

    @Test
    @DisplayName("创建预约-超过每日预约上限抛异常")
    void 创建预约超过上限() {
        when(tradeAppointmentLockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(validLock);
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.create(createRequest);
        });

        assertEquals(ErrorCode.APPOINTMENT_LIMIT.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("修改预约-正常流程")
    void 修改预约成功() {
        TradeAppointmentDO appointment = new TradeAppointmentDO();
        appointment.setId(1L);
        appointment.setAppointmentNo("APT20260605000001");
        appointment.setStatus(1);
        appointment.setMemberId(100L);
        appointment.setStoreId(1L);
        appointment.setTechnicianId(1L);
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setAppointmentTime(LocalTime.of(9, 0));
        appointment.setTimeSlot("09:00");
        appointment.setDurationMinutes(60);
        appointment.setVersion(0);

        when(tradeAppointmentMapper.selectById(1L)).thenReturn(appointment);
        when(tradeAppointmentMapper.updateById(any(TradeAppointmentDO.class))).thenReturn(1);

        AppointmentModifyRequest request = new AppointmentModifyRequest();
        request.setDate(LocalDate.now().plusDays(1));
        request.setTimeSlot("10:00");
        request.setTechnicianId(1L);
        request.setModifyReason("临时有事需要调整时间");

        R<AppointmentResponse> result = appointmentService.modify(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("APT20260605000001", result.getData().getAppointmentNo());
        verify(appointmentLockService, times(1)).lockTemp(any(), eq(100L));
    }

    @Test
    @DisplayName("修改预约-超过修改次数限制抛异常")
    void 修改预约超过限制() {
        TradeAppointmentDO appointment = new TradeAppointmentDO();
        appointment.setId(1L);
        appointment.setAppointmentNo("APT20260605000001");
        appointment.setStatus(1);
        appointment.setMemberId(100L);
        appointment.setStoreId(1L);
        appointment.setTechnicianId(1L);
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setAppointmentTime(LocalTime.of(9, 0));
        appointment.setTimeSlot("09:00");
        appointment.setDurationMinutes(60);
        appointment.setModifyReason("第一次修改原因");

        when(tradeAppointmentMapper.selectById(1L)).thenReturn(appointment);

        AppointmentModifyRequest request = new AppointmentModifyRequest();
        request.setDate(LocalDate.now().plusDays(1));
        request.setTimeSlot("10:00");
        request.setTechnicianId(1L);
        request.setModifyReason("第二次修改原因");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.modify(1L, request);
        });

        assertEquals(ErrorCode.APPOINTMENT_MODIFY_LIMIT.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("修改预约-预约不存在抛异常")
    void 修改预约不存在() {
        when(tradeAppointmentMapper.selectById(999L)).thenReturn(null);

        AppointmentModifyRequest request = new AppointmentModifyRequest();
        request.setDate(LocalDate.now().plusDays(1));
        request.setTimeSlot("10:00");
        request.setTechnicianId(1L);
        request.setModifyReason("修改原因");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.modify(999L, request);
        });

        assertEquals(ErrorCode.APPOINTMENT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("取消预约-正常流程")
    void 取消预约成功() {
        TradeAppointmentDO appointment = new TradeAppointmentDO();
        appointment.setId(1L);
        appointment.setAppointmentNo("APT20260605000001");
        appointment.setStatus(1);
        appointment.setMemberId(100L);
        appointment.setStoreId(1L);
        appointment.setTechnicianId(1L);
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setAppointmentTime(LocalTime.of(9, 0));
        appointment.setTimeSlot("09:00");
        appointment.setDurationMinutes(60);

        when(tradeAppointmentMapper.selectById(1L)).thenReturn(appointment);
        when(tradeAppointmentMapper.updateById(any(TradeAppointmentDO.class))).thenReturn(1);
        when(tradeAppointmentLockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        AppointmentCancelRequest request = new AppointmentCancelRequest();
        request.setCancelReason("临时有事无法到店");

        R<Void> result = appointmentService.cancel(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(5, appointment.getStatus());
        verify(tradeAppointmentMapper, times(1)).updateById(any(TradeAppointmentDO.class));
    }

    @Test
    @DisplayName("取消预约-不足2小时抛异常")
    void 取消预约时间不足() {
        TradeAppointmentDO appointment = new TradeAppointmentDO();
        appointment.setId(1L);
        appointment.setAppointmentNo("APT20260605000001");
        appointment.setStatus(1);
        appointment.setMemberId(100L);
        appointment.setStoreId(1L);
        appointment.setTechnicianId(1L);
        appointment.setAppointmentDate(LocalDate.now());
        appointment.setAppointmentTime(LocalTime.now().plusHours(1));
        appointment.setTimeSlot("09:00");

        when(tradeAppointmentMapper.selectById(1L)).thenReturn(appointment);

        AppointmentCancelRequest request = new AppointmentCancelRequest();
        request.setCancelReason("临时有事");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.cancel(1L, request);
        });

        assertEquals(ErrorCode.APPOINTMENT_CANCEL_TIMEOUT.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("取消预约-预约不存在抛异常")
    void 取消预约不存在() {
        when(tradeAppointmentMapper.selectById(999L)).thenReturn(null);

        AppointmentCancelRequest request = new AppointmentCancelRequest();
        request.setCancelReason("取消原因");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.cancel(999L, request);
        });

        assertEquals(ErrorCode.APPOINTMENT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("查询我的预约-返回全部分组")
    void 查询我的预约全部() {
        TradeAppointmentDO pending = new TradeAppointmentDO();
        pending.setId(1L);
        pending.setStatus(1);
        pending.setMemberId(100L);
        pending.setAppointmentDate(LocalDate.now().plusDays(1));
        pending.setAppointmentTime(LocalTime.of(9, 0));
        pending.setTimeSlot("09:00");

        TradeAppointmentDO completed = new TradeAppointmentDO();
        completed.setId(2L);
        completed.setStatus(4);
        completed.setMemberId(100L);
        completed.setAppointmentDate(LocalDate.now().minusDays(1));
        completed.setAppointmentTime(LocalTime.of(10, 0));
        completed.setTimeSlot("10:00");

        when(tradeAppointmentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pending, completed));

        R<MyAppointmentVO> result = appointmentService.getMyAppointments(100L, "all");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData().getPending());
        assertNotNull(result.getData().getCompleted());
        assertEquals(1, result.getData().getPending().getCount());
        assertEquals(1, result.getData().getCompleted().getCount());
    }

    @Test
    @DisplayName("根据ID查询预约-不存在抛异常")
    void 根据ID查询预约不存在() {
        when(tradeAppointmentMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.getById(999L);
        });

        assertEquals(ErrorCode.APPOINTMENT_NOT_FOUND.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("预约不存在"));
    }
}
