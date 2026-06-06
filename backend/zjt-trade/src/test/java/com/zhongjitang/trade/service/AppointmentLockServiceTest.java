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
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("预约锁档服务测试")
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
    @DisplayName("临时锁档-正常流程")
    void 临时锁档成功() {
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
    @DisplayName("临时锁档-时段已被预约占用抛异常")
    void 临时锁档时段已被预约() {
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentLockService.lockTemp(lockTempRequest, 100L);
        });

        assertEquals(ErrorCode.SLOT_LOCKED.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("时段"));
        verify(tradeAppointmentLockMapper, never()).insert(any());
    }

    @Test
    @DisplayName("临时锁档-时段已被临时锁定抛异常")
    void 临时锁档时段已被临时锁定() {
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeAppointmentLockMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentLockService.lockTemp(lockTempRequest, 100L);
        });

        assertEquals(ErrorCode.SLOT_TEMP_LOCKED.getCode(), exception.getCode());
        verify(tradeAppointmentLockMapper, never()).insert(any());
    }

    @Test
    @DisplayName("临时锁档-获取分布式锁失败抛异常")
    void 临时锁档获取分布式锁失败() {
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeAppointmentLockMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(distributedLockUtil.lock(anyString(), anyLong(), anyInt(), anyLong())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentLockService.lockTemp(lockTempRequest, 100L);
        });

        assertEquals(ErrorCode.LOCK_ACQUIRE_FAILED.getCode(), exception.getCode());
        verify(tradeAppointmentLockMapper, never()).insert(any());
    }

    @Test
    @DisplayName("释放锁-正常流程")
    void 释放锁成功() {
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

        assertEquals(2, lockDO.getStatus());
        verify(tradeAppointmentLockMapper).updateById(any(TradeAppointmentLockDO.class));
        verify(redisUtil).delete(anyString());
        verify(distributedLockUtil).unlock(anyString());
    }

    @Test
    @DisplayName("释放锁-锁记录不存在不执行操作")
    void 释放锁不存在() {
        when(tradeAppointmentLockMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        appointmentLockService.releaseLock("LOCK_NOTEXIST");

        verify(tradeAppointmentLockMapper, never()).updateById(any());
        verify(redisUtil, never()).delete(anyString());
        verify(distributedLockUtil, never()).unlock(anyString());
    }

    @Test
    @DisplayName("临时锁档-插入锁记录异常时释放分布式锁")
    void 临时锁档插入异常时释放分布式锁() {
        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeAppointmentLockMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(distributedLockUtil.lock(anyString(), anyLong(), anyInt(), anyLong())).thenReturn(true);
        when(tradeAppointmentLockMapper.insert(any(TradeAppointmentLockDO.class))).thenThrow(new RuntimeException("DB错误"));

        assertThrows(RuntimeException.class, () -> {
            appointmentLockService.lockTemp(lockTempRequest, 100L);
        });

        verify(distributedLockUtil).unlock(anyString());
    }

    @Test
    @DisplayName("临时锁档-technicianId为0时不按技师过滤")
    void 临时锁档不指定技师() {
        lockTempRequest.setTechnicianId(0L);

        when(tradeAppointmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeAppointmentLockMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(distributedLockUtil.lock(anyString(), anyLong(), anyInt(), anyLong())).thenReturn(true);
        when(tradeAppointmentLockMapper.insert(any(TradeAppointmentLockDO.class))).thenReturn(1);

        R<LockResultVO> result = appointmentLockService.lockTemp(lockTempRequest, 100L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
    }
}
