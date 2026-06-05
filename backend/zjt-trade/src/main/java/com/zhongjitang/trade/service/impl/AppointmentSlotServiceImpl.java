package com.zhongjitang.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.common.redis.util.RedisUtil;
import com.zhongjitang.trade.domain.entity.TradeAppointmentDO;
import com.zhongjitang.trade.domain.entity.TradeAppointmentLockDO;
import com.zhongjitang.trade.domain.vo.AvailableSlotVO;
import com.zhongjitang.trade.mapper.TradeAppointmentLockMapper;
import com.zhongjitang.trade.mapper.TradeAppointmentMapper;
import com.zhongjitang.trade.service.AppointmentSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentSlotServiceImpl implements AppointmentSlotService {

    private final TradeAppointmentMapper tradeAppointmentMapper;
    private final TradeAppointmentLockMapper tradeAppointmentLockMapper;
    private final RedisUtil redisUtil;

    private static final String SLOT_CACHE_PREFIX = "slot:";
    private static final long CACHE_EXPIRE_MINUTES = 5;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public R<AvailableSlotVO> getAvailableSlots(Long storeId, Long technicianId, LocalDate date) {
        String cacheKey = SLOT_CACHE_PREFIX + storeId + ":" + technicianId + ":" + date;

        // 1. 尝试从Redis获取缓存
        Object cached = redisUtil.get(cacheKey);
        if (cached instanceof AvailableSlotVO) {
            return R.ok((AvailableSlotVO) cached);
        }

        // 2. 生成时段列表
        AvailableSlotVO result = new AvailableSlotVO();
        result.setStoreId(storeId);
        result.setDate(date);
        result.setTechnicianId(technicianId);

        List<AvailableSlotVO.TimeSlotItem> timeSlots = generateTimeSlots();
        result.setTimeSlots(timeSlots);

        // 3. 查询已占用时段
        List<String> occupiedSlots = getOccupiedSlots(storeId, technicianId, date);

        // 4. 查询临时锁定时段
        List<String> tempLockedSlots = getTempLockedSlots(storeId, technicianId, date);

        // 5. 标记available和remainingCount
        for (AvailableSlotVO.TimeSlotItem item : timeSlots) {
            boolean isOccupied = occupiedSlots.contains(item.getTimeSlot());
            boolean isTempLocked = tempLockedSlots.contains(item.getTimeSlot());
            item.setAvailable(!isOccupied && !isTempLocked);
            if (technicianId == 0) {
                item.setRemainingCount(isOccupied ? 0 : (isTempLocked ? 0 : 10));
            } else {
                item.setRemainingCount(isOccupied ? 0 : (isTempLocked ? 0 : 1));
            }
        }

        // 6. 写入Redis缓存(5分钟过期)
        redisUtil.setWithExpire(cacheKey, result, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return R.ok(result);
    }

    private List<AvailableSlotVO.TimeSlotItem> generateTimeSlots() {
        List<AvailableSlotVO.TimeSlotItem> slots = new ArrayList<>();
        LocalTime businessStart = LocalTime.of(9, 0);
        LocalTime businessEnd = LocalTime.of(21, 0);
        int slotDurationMinutes = 60;

        LocalTime current = businessStart;
        while (current.isBefore(businessEnd)) {
            AvailableSlotVO.TimeSlotItem item = new AvailableSlotVO.TimeSlotItem();
            item.setTimeSlot(current.format(TIME_FORMATTER));
            item.setAvailable(true);
            item.setRemainingCount(1);
            slots.add(item);
            current = current.plusMinutes(slotDurationMinutes);
        }
        return slots;
    }

    private List<String> getOccupiedSlots(Long storeId, Long technicianId, LocalDate date) {
        LambdaQueryWrapper<TradeAppointmentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAppointmentDO::getStoreId, storeId)
                .eq(TradeAppointmentDO::getAppointmentDate, date)
                .in(TradeAppointmentDO::getStatus, Arrays.asList(1, 2, 3));

        if (technicianId != 0) {
            wrapper.eq(TradeAppointmentDO::getTechnicianId, technicianId);
        }

        List<TradeAppointmentDO> appointments = tradeAppointmentMapper.selectList(wrapper);
        return appointments.stream()
                .map(TradeAppointmentDO::getTimeSlot)
                .collect(Collectors.toList());
    }

    private List<String> getTempLockedSlots(Long storeId, Long technicianId, LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<TradeAppointmentLockDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeAppointmentLockDO::getStoreId, storeId)
                .eq(TradeAppointmentLockDO::getLockDate, date)
                .eq(TradeAppointmentLockDO::getStatus, 1)
                .gt(TradeAppointmentLockDO::getExpireAt, now);

        if (technicianId != 0) {
            wrapper.eq(TradeAppointmentLockDO::getTechnicianId, technicianId);
        }

        List<TradeAppointmentLockDO> locks = tradeAppointmentLockMapper.selectList(wrapper);
        return locks.stream()
                .map(TradeAppointmentLockDO::getTimeSlot)
                .collect(Collectors.toList());
    }
}
