package com.zhongjitang.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

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
    void testCreateAppointmentSuccess() {
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

        verify(tradeAppointmentMapper, times(1)).insert(any(TradeAppointmentDO.class));
        verify(tradeAppointmentLockMapper, times(1)).updateById(any(TradeAppointmentLockDO.class));
        verify(redisUtil, times(2)).delete(anyString());
    }

    @Test
    void testCreateAppointmentLockExpired() {
        validLock.setExpireAt(LocalDateTime.now().minusMinutes(1));
        when(tradeAppointmentLockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.create(createRequest);
        });
        assertEquals(ErrorCode.SLOT_TEMP_LOCKED.getCode(), exception.getCode());
    }

    @Test
    void testCreateAppointmentDuplicateLimit() {
        when(tradeAppointmentLockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(validLock);
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.create(createRequest);
        });
        assertEquals(ErrorCode.APPOINTMENT_LIMIT.getCode(), exception.getCode());
    }

    @Test
    void testGenerateAppointmentNo() {
        when(tradeAppointmentLockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(validLock);
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeAppointmentMapper.insert(any(TradeAppointmentDO.class))).thenReturn(1);
        when(tradeAppointmentLockMapper.updateById(any(TradeAppointmentLockDO.class))).thenReturn(1);

        R<AppointmentResponse> result = appointmentService.create(createRequest);

        assertNotNull(result.getData());
        String appointmentNo = result.getData().getAppointmentNo();
        assertTrue(appointmentNo.startsWith("APT"));
        assertEquals(17, appointmentNo.length()); // APT + yyyyMMdd + 6位序号
    }

    @Test
    void testGetByIdNotFound() {
        when(tradeAppointmentMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.getById(999L);
        });
        assertEquals(ErrorCode.APPOINTMENT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testGetByIdSuccess() {
        TradeAppointmentDO appointment = new TradeAppointmentDO();
        appointment.setId(1L);
        appointment.setAppointmentNo("APT20260605000001");
        appointment.setStatus(1);
        when(tradeAppointmentMapper.selectById(1L)).thenReturn(appointment);

        R<TradeAppointmentDO> result = appointmentService.getById(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("APT20260605000001", result.getData().getAppointmentNo());
    }

    // ========== 我的预约列表测试 ==========

    @Test
    void testGetMyAppointmentsAll() {
        TradeAppointmentDO pendingAppointment = new TradeAppointmentDO();
        pendingAppointment.setId(1L);
        pendingAppointment.setAppointmentNo("APT20260605000001");
        pendingAppointment.setStatus(1);
        pendingAppointment.setMemberId(100L);
        pendingAppointment.setAppointmentDate(LocalDate.now().plusDays(1));
        pendingAppointment.setAppointmentTime(LocalTime.of(9, 0));
        pendingAppointment.setTimeSlot("09:00");

        TradeAppointmentDO completedAppointment = new TradeAppointmentDO();
        completedAppointment.setId(2L);
        completedAppointment.setAppointmentNo("APT20260605000002");
        completedAppointment.setStatus(4);
        completedAppointment.setMemberId(100L);
        completedAppointment.setAppointmentDate(LocalDate.now().minusDays(1));
        completedAppointment.setAppointmentTime(LocalTime.of(10, 0));
        completedAppointment.setTimeSlot("10:00");

        TradeAppointmentDO cancelledAppointment = new TradeAppointmentDO();
        cancelledAppointment.setId(3L);
        cancelledAppointment.setAppointmentNo("APT20260605000003");
        cancelledAppointment.setStatus(5);
        cancelledAppointment.setMemberId(100L);
        cancelledAppointment.setAppointmentDate(LocalDate.now().minusDays(2));
        cancelledAppointment.setAppointmentTime(LocalTime.of(11, 0));
        cancelledAppointment.setTimeSlot("11:00");

        when(tradeAppointmentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pendingAppointment, completedAppointment, cancelledAppointment));

        R<MyAppointmentVO> result = appointmentService.getMyAppointments(100L, "all");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getPending());
        assertNotNull(result.getData().getCompleted());
        assertNotNull(result.getData().getCancelled());
        assertEquals(1, result.getData().getPending().getCount());
        assertEquals(1, result.getData().getCompleted().getCount());
        assertEquals(1, result.getData().getCancelled().getCount());
    }

    @Test
    void testGetMyAppointmentsPendingOnly() {
        TradeAppointmentDO pendingAppointment = new TradeAppointmentDO();
        pendingAppointment.setId(1L);
        pendingAppointment.setAppointmentNo("APT20260605000001");
        pendingAppointment.setStatus(2);
        pendingAppointment.setMemberId(100L);
        pendingAppointment.setAppointmentDate(LocalDate.now().plusDays(1));
        pendingAppointment.setAppointmentTime(LocalTime.of(9, 0));
        pendingAppointment.setTimeSlot("09:00");

        when(tradeAppointmentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(pendingAppointment));

        R<MyAppointmentVO> result = appointmentService.getMyAppointments(100L, "pending");

        assertNotNull(result);
        assertNotNull(result.getData().getPending());
        assertEquals(1, result.getData().getPending().getCount());
        assertNull(result.getData().getCompleted());
        assertNull(result.getData().getCancelled());
    }

    @Test
    void testGetMyAppointmentsCanModifyAndCanCancel() {
        TradeAppointmentDO appointment = new TradeAppointmentDO();
        appointment.setId(1L);
        appointment.setAppointmentNo("APT20260605000001");
        appointment.setStatus(1);
        appointment.setMemberId(100L);
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setAppointmentTime(LocalTime.of(9, 0));
        appointment.setTimeSlot("09:00");

        when(tradeAppointmentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(appointment));

        R<MyAppointmentVO> result = appointmentService.getMyAppointments(100L, "pending");

        assertNotNull(result);
        MyAppointmentVO.AppointmentItemVO item = result.getData().getPending().getList().get(0);
        assertTrue(item.getCanModify());
        assertTrue(item.getCanCancel());
        assertEquals(0, item.getModifyCount());
    }

    // ========== 修改预约测试 ==========

    @Test
    void testModifyAppointmentSuccess() {
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
        verify(tradeAppointmentMapper, times(1)).updateById(any(TradeAppointmentDO.class));
    }

    @Test
    void testModifyAppointmentExceedLimit() {
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
        appointment.setVersion(2);
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
    void testModifyAppointmentExpired() {
        TradeAppointmentDO appointment = new TradeAppointmentDO();
        appointment.setId(1L);
        appointment.setAppointmentNo("APT20260605000001");
        appointment.setStatus(5);
        appointment.setMemberId(100L);
        appointment.setStoreId(1L);
        appointment.setTechnicianId(1L);
        appointment.setAppointmentDate(LocalDate.now().minusDays(1));
        appointment.setAppointmentTime(LocalTime.of(9, 0));
        appointment.setTimeSlot("09:00");
        appointment.setDurationMinutes(60);

        when(tradeAppointmentMapper.selectById(1L)).thenReturn(appointment);

        AppointmentModifyRequest request = new AppointmentModifyRequest();
        request.setDate(LocalDate.now().plusDays(1));
        request.setTimeSlot("10:00");
        request.setTechnicianId(1L);
        request.setModifyReason("修改原因");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.modify(1L, request);
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testModifyAppointmentNotFound() {
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

    // ========== 取消预约测试 ==========

    @Test
    void testCancelAppointmentSuccess() {
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

        verify(tradeAppointmentMapper, times(1)).updateById(any(TradeAppointmentDO.class));
        verify(redisUtil, atLeastOnce()).delete(anyString());
    }

    @Test
    void testCancelAppointmentWithin2Hours() {
        TradeAppointmentDO appointment = new TradeAppointmentDO();
        appointment.setId(1L);
        appointment.setAppointmentNo("APT20260605000001");
        appointment.setStatus(1);
        appointment.setMemberId(100L);
        appointment.setStoreId(1L);
        appointment.setTechnicianId(1L);
        // 预约时间在1小时后，不足2小时
        appointment.setAppointmentDate(LocalDate.now());
        appointment.setAppointmentTime(LocalTime.now().plusHours(1));
        appointment.setTimeSlot("09:00");
        appointment.setDurationMinutes(60);

        when(tradeAppointmentMapper.selectById(1L)).thenReturn(appointment);

        AppointmentCancelRequest request = new AppointmentCancelRequest();
        request.setCancelReason("临时有事无法到店");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.cancel(1L, request);
        });
        assertEquals(ErrorCode.APPOINTMENT_CANCEL_TIMEOUT.getCode(), exception.getCode());
    }

    @Test
    void testCancelAppointmentNotFound() {
        when(tradeAppointmentMapper.selectById(999L)).thenReturn(null);

        AppointmentCancelRequest request = new AppointmentCancelRequest();
        request.setCancelReason("取消原因");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.cancel(999L, request);
        });
        assertEquals(ErrorCode.APPOINTMENT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testCancelAppointmentAlreadyCancelled() {
        TradeAppointmentDO appointment = new TradeAppointmentDO();
        appointment.setId(1L);
        appointment.setAppointmentNo("APT20260605000001");
        appointment.setStatus(5);
        appointment.setMemberId(100L);
        appointment.setStoreId(1L);
        appointment.setTechnicianId(1L);
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setAppointmentTime(LocalTime.of(9, 0));
        appointment.setTimeSlot("09:00");

        when(tradeAppointmentMapper.selectById(1L)).thenReturn(appointment);

        AppointmentCancelRequest request = new AppointmentCancelRequest();
        request.setCancelReason("再次取消");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.cancel(1L, request);
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }
}
