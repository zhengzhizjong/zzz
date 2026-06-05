package com.zhongjitang.trade.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhongjitang.common.redis.util.RedisUtil;
import com.zhongjitang.trade.domain.entity.TradeAppointmentDO;
import com.zhongjitang.trade.mapper.TradeAppointmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentExpireJob {

    private final TradeAppointmentMapper tradeAppointmentMapper;
    private final RedisUtil redisUtil;

    private static final String SLOT_CACHE_PREFIX = "slot:";
    private static final String REDIS_LOCK_PREFIX = "lock:";

    @Scheduled(cron = "0 5 0 * * ?")
    public void expireAppointments() {
        log.info("开始执行过期预约定时任务...");

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        // 查询需要过期的预约：
        // 1. 预约日期 < 今天（已过期的日期）
        // 2. 或者预约日期 = 今天 且 预约时间 < 当前时间
        // 且状态为1(待确认)或2(已确认)
        LambdaQueryWrapper<TradeAppointmentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TradeAppointmentDO::getStatus, Arrays.asList(1, 2))
                .and(w -> w
                        .lt(TradeAppointmentDO::getAppointmentDate, today)
                        .or()
                        .nested(n -> n.eq(TradeAppointmentDO::getAppointmentDate, today)
                                .lt(TradeAppointmentDO::getAppointmentTime, nowTime))
                );

        List<TradeAppointmentDO> expiredAppointments = tradeAppointmentMapper.selectList(wrapper);

        if (expiredAppointments.isEmpty()) {
            log.info("没有需要过期的预约");
            return;
        }

        log.info("发现{}条过期预约需要处理", expiredAppointments.size());

        for (TradeAppointmentDO appointment : expiredAppointments) {
            try {
                // 更新状态为6(超时未到)
                LambdaUpdateWrapper<TradeAppointmentDO> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(TradeAppointmentDO::getId, appointment.getId())
                        .in(TradeAppointmentDO::getStatus, Arrays.asList(1, 2))
                        .set(TradeAppointmentDO::getStatus, 6);
                tradeAppointmentMapper.update(null, updateWrapper);

                // 释放对应时段Redis缓存
                releaseSlotCache(appointment);

                log.info("预约[{}]已标记为超时未到", appointment.getAppointmentNo());
            } catch (Exception e) {
                log.error("处理过期预约[{}]失败: {}", appointment.getAppointmentNo(), e.getMessage(), e);
            }
        }

        log.info("过期预约定时任务执行完成，共处理{}条", expiredAppointments.size());
    }

    private void releaseSlotCache(TradeAppointmentDO appointment) {
        Long storeId = appointment.getStoreId();
        Long technicianId = appointment.getTechnicianId();
        LocalDate date = appointment.getAppointmentDate();
        String timeSlot = appointment.getTimeSlot();

        // 删除Redis锁标记
        String redisLockKey = REDIS_LOCK_PREFIX + storeId + ":" + technicianId + ":" + date + ":" + timeSlot;
        redisUtil.delete(redisLockKey);

        // 删除时段缓存
        String slotCacheKey = SLOT_CACHE_PREFIX + storeId + ":" + technicianId + ":" + date;
        redisUtil.delete(slotCacheKey);
    }
}
