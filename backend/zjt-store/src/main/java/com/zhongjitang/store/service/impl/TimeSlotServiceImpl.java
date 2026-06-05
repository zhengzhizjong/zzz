package com.zhongjitang.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.TimeSlotConfigCreateRequest;
import com.zhongjitang.store.domain.dto.TimeSlotConfigUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreTimeSlotConfigDO;
import com.zhongjitang.store.domain.vo.TimeSlotVO;
import com.zhongjitang.store.mapper.StoreTimeSlotConfigMapper;
import com.zhongjitang.store.service.ITimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements ITimeSlotService {

    private final StoreTimeSlotConfigMapper timeSlotConfigMapper;

    @Override
    public R<StoreTimeSlotConfigDO> getByStoreId(Long storeId) {
        LambdaQueryWrapper<StoreTimeSlotConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreTimeSlotConfigDO::getStoreId, storeId);
        StoreTimeSlotConfigDO config = timeSlotConfigMapper.selectOne(wrapper);
        if (config == null) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND, "门店时段配置不存在，门店ID: " + storeId);
        }
        return R.ok(config);
    }

    @Override
    public R<Void> create(TimeSlotConfigCreateRequest request) {
        StoreTimeSlotConfigDO config = new StoreTimeSlotConfigDO();
        config.setStoreId(request.getStoreId());
        config.setBusinessStartTime(request.getBusinessStartTime());
        config.setBusinessEndTime(request.getBusinessEndTime());
        config.setSlotDurationMinutes(request.getSlotDurationMinutes());
        config.setRestStartTime(request.getRestStartTime());
        config.setRestEndTime(request.getRestEndTime());
        config.setStatus(1);
        timeSlotConfigMapper.insert(config);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, TimeSlotConfigUpdateRequest request) {
        StoreTimeSlotConfigDO config = timeSlotConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "时段配置不存在，ID: " + id);
        }
        if (request.getBusinessStartTime() != null) {
            config.setBusinessStartTime(request.getBusinessStartTime());
        }
        if (request.getBusinessEndTime() != null) {
            config.setBusinessEndTime(request.getBusinessEndTime());
        }
        if (request.getSlotDurationMinutes() != null) {
            config.setSlotDurationMinutes(request.getSlotDurationMinutes());
        }
        if (request.getRestStartTime() != null) {
            config.setRestStartTime(request.getRestStartTime());
        }
        if (request.getRestEndTime() != null) {
            config.setRestEndTime(request.getRestEndTime());
        }
        timeSlotConfigMapper.updateById(config);
        return R.ok();
    }

    @Override
    public R<List<TimeSlotVO>> getAvailableSlots(Long storeId, Long technicianId, LocalDate date) {
        LambdaQueryWrapper<StoreTimeSlotConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreTimeSlotConfigDO::getStoreId, storeId);
        StoreTimeSlotConfigDO config = timeSlotConfigMapper.selectOne(wrapper);
        if (config == null) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND, "门店时段配置不存在，门店ID: " + storeId);
        }

        List<TimeSlotVO> slots = generateTimeSlots(config);

        // TODO: 查询trade_appointment表获取已占用时段，标记available和remainingCount
        // 当前暂时标记所有时段为可用，remainingCount为门店技师总数
        for (TimeSlotVO slot : slots) {
            slot.setAvailable(true);
            slot.setRemainingCount(0);
        }

        return R.ok(slots);
    }

    private List<TimeSlotVO> generateTimeSlots(StoreTimeSlotConfigDO config) {
        List<TimeSlotVO> slots = new ArrayList<>();
        LocalTime startTime = config.getBusinessStartTime();
        LocalTime endTime = config.getBusinessEndTime();
        int duration = config.getSlotDurationMinutes();
        LocalTime restStart = config.getRestStartTime();
        LocalTime restEnd = config.getRestEndTime();

        LocalTime current = startTime;
        while (current.plusMinutes(duration).compareTo(endTime) <= 0) {
            if (!isInRestPeriod(current, duration, restStart, restEnd)) {
                TimeSlotVO slot = new TimeSlotVO();
                slot.setTimeSlot(current.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                slot.setAvailable(true);
                slot.setRemainingCount(0);
                slots.add(slot);
            }
            current = current.plusMinutes(duration);
        }
        return slots;
    }

    private boolean isInRestPeriod(LocalTime slotStart, int duration, LocalTime restStart, LocalTime restEnd) {
        if (restStart == null || restEnd == null) {
            return false;
        }
        LocalTime slotEnd = slotStart.plusMinutes(duration);
        return slotStart.compareTo(restEnd) < 0 && slotEnd.compareTo(restStart) > 0;
    }
}
