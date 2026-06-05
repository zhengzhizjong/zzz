package com.zhongjitang.trade.service.impl;

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
import com.zhongjitang.trade.service.AppointmentLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentLockServiceImpl implements AppointmentLockService {

    private final DistributedLockUtil distributedLockUtil;
    private final RedisUtil redisUtil;
    private final TradeAppointmentLockMapper tradeAppointmentLockMapper;
    private final TradeAppointmentMapper tradeAppointmentMapper;

    private static final String LOCK_KEY_PREFIX = "appointment:";
    private static final String REDIS_LOCK_PREFIX = "lock:";
    private static final long LOCK_EXPIRE_MINUTES = 5;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public R<LockResultVO> lockTemp(LockTempRequest request, Long memberId) {
        Long storeId = request.getStoreId();
        Long technicianId = request.getTechnicianId();
        LocalDate date = request.getDate();
        String timeSlot = request.getTimeSlot();

        // 1. 检查该时段是否已有有效预约
        LambdaQueryWrapper<TradeAppointmentDO> appointmentWrapper = new LambdaQueryWrapper<>();
        appointmentWrapper.eq(TradeAppointmentDO::getStoreId, storeId)
                .eq(TradeAppointmentDO::getAppointmentDate, date)
                .eq(TradeAppointmentDO::getTimeSlot, timeSlot)
                .in(TradeAppointmentDO::getStatus, Arrays.asList(1, 2, 3));
        if (technicianId != 0) {
            appointmentWrapper.eq(TradeAppointmentDO::getTechnicianId, technicianId);
        }
        Long appointmentCount = tradeAppointmentMapper.selectCount(appointmentWrapper);
        if (appointmentCount > 0) {
            throw new BusinessException(ErrorCode.SLOT_LOCKED, "时段: " + timeSlot);
        }

        // 2. 检查该时段是否已有有效锁
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<TradeAppointmentLockDO> lockWrapper = new LambdaQueryWrapper<>();
        lockWrapper.eq(TradeAppointmentLockDO::getStoreId, storeId)
                .eq(TradeAppointmentLockDO::getLockDate, date)
                .eq(TradeAppointmentLockDO::getTimeSlot, timeSlot)
                .eq(TradeAppointmentLockDO::getStatus, 1)
                .gt(TradeAppointmentLockDO::getExpireAt, now);
        if (technicianId != 0) {
            lockWrapper.eq(TradeAppointmentLockDO::getTechnicianId, technicianId);
        }
        Long lockCount = tradeAppointmentLockMapper.selectCount(lockWrapper);
        if (lockCount > 0) {
            throw new BusinessException(ErrorCode.SLOT_TEMP_LOCKED, "时段: " + timeSlot);
        }

        // 3. 使用DistributedLockUtil获取分布式锁
        String lockKey = LOCK_KEY_PREFIX + storeId + ":" + technicianId + ":" + date + ":" + timeSlot;
        boolean locked = distributedLockUtil.lock(lockKey, LOCK_EXPIRE_MINUTES * 60, 3, 200);
        if (!locked) {
            throw new BusinessException(ErrorCode.LOCK_ACQUIRE_FAILED);
        }

        try {
            // 4. 在trade_appointment_lock表插入锁记录
            String lockId = "LOCK_" + date.format(DATE_FORMATTER) + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            LocalDateTime expireAt = now.plusMinutes(LOCK_EXPIRE_MINUTES);

            TradeAppointmentLockDO lockDO = new TradeAppointmentLockDO();
            lockDO.setLockId(lockId);
            lockDO.setTechnicianId(technicianId);
            lockDO.setLockDate(date);
            lockDO.setTimeSlot(timeSlot);
            lockDO.setMemberId(memberId);
            lockDO.setSessionId(UUID.randomUUID().toString());
            lockDO.setExpireAt(expireAt);
            lockDO.setStatus(1);
            lockDO.setStoreId(storeId);
            tradeAppointmentLockMapper.insert(lockDO);

            // 5. 在Redis中设置锁标记
            String redisLockKey = REDIS_LOCK_PREFIX + storeId + ":" + technicianId + ":" + date + ":" + timeSlot;
            redisUtil.setWithExpire(redisLockKey, lockId, LOCK_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 6. 返回LockResultVO
            LockResultVO result = new LockResultVO();
            result.setLockId(lockId);
            result.setStoreId(storeId);
            result.setTechnicianId(technicianId);
            result.setDate(date);
            result.setTimeSlot(timeSlot);
            result.setExpireAt(expireAt);

            return R.ok(result);
        } catch (Exception e) {
            distributedLockUtil.unlock(lockKey);
            throw e;
        }
    }

    @Override
    public void releaseLock(String lockId) {
        // 1. 查询锁记录
        LambdaQueryWrapper<TradeAppointmentLockDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAppointmentLockDO::getLockId, lockId)
                .eq(TradeAppointmentLockDO::getStatus, 1);
        TradeAppointmentLockDO lockDO = tradeAppointmentLockMapper.selectOne(wrapper);
        if (lockDO == null) {
            return;
        }

        // 2. 更新lock记录status=2(已释放)
        lockDO.setStatus(2);
        tradeAppointmentLockMapper.updateById(lockDO);

        // 3. 删除Redis锁标记
        String redisLockKey = REDIS_LOCK_PREFIX + lockDO.getStoreId() + ":"
                + lockDO.getTechnicianId() + ":" + lockDO.getLockDate() + ":" + lockDO.getTimeSlot();
        redisUtil.delete(redisLockKey);

        // 4. 释放分布式锁
        String lockKey = LOCK_KEY_PREFIX + lockDO.getStoreId() + ":"
                + lockDO.getTechnicianId() + ":" + lockDO.getLockDate() + ":" + lockDO.getTimeSlot();
        distributedLockUtil.unlock(lockKey);
    }
}
