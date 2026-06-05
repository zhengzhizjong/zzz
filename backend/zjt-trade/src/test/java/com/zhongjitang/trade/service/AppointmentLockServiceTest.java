package com.zhongjitang.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.common.redis.util.DistributedLockUtil;
import com.zhongjitang.common.redis.util.RedisUtil;
import com.zhongjitang.trade.domain.dto.LockTempRequest;
import com.zhongjitang.trade.domain.entity.TradeAppointmentDO;
import com.zhongjitang.trade.domain.entity.TradeAppointmentLockDO;
import com.zhongjitang.trade.domain.vo.LockResultVO;
import com.zhongjitang.trade.mapper.TradeAppointmentLockMapper;
import com.zhongjitang.trade.mapper.TradeAppointmentMapper;
import com.zhongjitang.trade.service.impl.AppointmentLockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentLockServiceTest {

    @Mock
    private DistributedLockUtil distributedLockUtil;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private TradeAppointmentLockMapper tradeAppointmentLockMapper;

    @Mock
    private TradeAppointmentMapper tradeAppointmentMapper;

    @InjectMocks
    private AppointmentLockServiceImpl appointmentLockService;

    private LockTempRequest lockTempRequest;

    @BeforeEach
    void setUp() {
        lockTempRequest = new LockTempRequest();
        lockTempRequest.setStoreId(1L);
        lockTempRequest.setTechnicianId(1L);
        lockTempRequest.setDate(LocalDate.of(2026, 6, 5));
        lockTempRequest.setTimeSlot("09:00");
    }

    @Test
    void testLockTempSuccess() {
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeAppointmentLockMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(distributedLockUtil.lock(anyString(), anyLong(), anyInt(), anyLong())).thenReturn(true);
        when(tradeAppointmentLockMapper.insert(any(TradeAppointmentLockDO.class))).thenReturn(1);

        R<LockResultVO> result = appointmentLockService.lockTemp(lockTempRequest, 100L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getLockId().startsWith("LOCK_"));
        assertEquals(1L, result.getData().getStoreId());
        assertEquals(1L, result.getData().getTechnicianId());
        assertNotNull(result.getData().getExpireAt());

        verify(redisUtil).setWithExpire(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testLockTempSlotOccupied() {
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentLockService.lockTemp(lockTempRequest, 100L);
        });
        assertEquals(ErrorCode.SLOT_LOCKED.getCode(), exception.getCode());

        verify(tradeAppointmentLockMapper, never()).insert(any());
    }

    @Test
    void testLockTempSlotTempLocked() {
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeAppointmentLockMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentLockService.lockTemp(lockTempRequest, 100L);
        });
        assertEquals(ErrorCode.SLOT_TEMP_LOCKED.getCode(), exception.getCode());

        verify(tradeAppointmentLockMapper, never()).insert(any());
    }

    @Test
    void testLockTempAcquireLockFailed() {
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeAppointmentLockMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(distributedLockUtil.lock(anyString(), anyLong(), anyInt(), anyLong())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentLockService.lockTemp(lockTempRequest, 100L);
        });
        assertEquals(ErrorCode.LOCK_ACQUIRE_FAILED.getCode(), exception.getCode());
    }

    @Test
    void testReleaseLockSuccess() {
        TradeAppointmentLockDO lockDO = new TradeAppointmentLockDO();
        lockDO.setId(1L);
        lockDO.setLockId("LOCK_20260605_ABCD1234");
        lockDO.setStoreId(1L);
        lockDO.setTechnicianId(1L);
        lockDO.setLockDate(LocalDate.of(2026, 6, 5));
        lockDO.setTimeSlot("09:00");
        lockDO.setStatus(1);

        when(tradeAppointmentLockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(lockDO);
        when(tradeAppointmentLockMapper.updateById(any(TradeAppointmentLockDO.class))).thenReturn(1);

        appointmentLockService.releaseLock("LOCK_20260605_ABCD1234");

        verify(tradeAppointmentLockMapper).updateById(any(TradeAppointmentLockDO.class));
        verify(redisUtil).delete(anyString());
        verify(distributedLockUtil).unlock(anyString());
    }

    @Test
    void testReleaseLockNotFound() {
        when(tradeAppointmentLockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        appointmentLockService.releaseLock("LOCK_NOTEXIST");

        verify(tradeAppointmentLockMapper, never()).updateById(any());
    }
}
