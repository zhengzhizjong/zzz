package com.zhongjitang.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.ScheduleBatchCreateRequest;
import com.zhongjitang.store.domain.dto.ScheduleCreateRequest;
import com.zhongjitang.store.domain.dto.ScheduleUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreScheduleDO;
import com.zhongjitang.store.mapper.StoreScheduleMapper;
import com.zhongjitang.store.service.IScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements IScheduleService {

    private final StoreScheduleMapper scheduleMapper;

    @Override
    public R<List<StoreScheduleDO>> getByTechnicianAndMonth(Long technicianId, String month) {
        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        LambdaQueryWrapper<StoreScheduleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreScheduleDO::getTechnicianId, technicianId);
        wrapper.ge(StoreScheduleDO::getScheduleDate, startDate);
        wrapper.le(StoreScheduleDO::getScheduleDate, endDate);
        wrapper.orderByAsc(StoreScheduleDO::getScheduleDate);
        List<StoreScheduleDO> schedules = scheduleMapper.selectList(wrapper);
        return R.ok(schedules);
    }

    @Override
    public R<List<StoreScheduleDO>> getByStoreAndDate(Long storeId, LocalDate date) {
        LambdaQueryWrapper<StoreScheduleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreScheduleDO::getStoreId, storeId);
        wrapper.eq(StoreScheduleDO::getScheduleDate, date);
        wrapper.orderByAsc(StoreScheduleDO::getStartTime);
        List<StoreScheduleDO> schedules = scheduleMapper.selectList(wrapper);
        return R.ok(schedules);
    }

    @Override
    public R<Void> create(ScheduleCreateRequest request) {
        StoreScheduleDO schedule = new StoreScheduleDO();
        schedule.setTechnicianId(request.getTechnicianId());
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setScheduleType(request.getScheduleType());
        schedule.setNotes(request.getNotes());
        scheduleMapper.insert(schedule);
        return R.ok();
    }

    @Override
    public R<Void> batchCreate(ScheduleBatchCreateRequest request) {
        List<StoreScheduleDO> schedules = new ArrayList<>();
        LocalDate current = request.getStartDate();

        while (!current.isAfter(request.getEndDate())) {
            java.time.DayOfWeek dayOfWeek = current.getDayOfWeek();
            int dayValue = dayOfWeek.getValue() % 7;
            boolean isRestDay = request.getRestDays() != null && request.getRestDays().contains(dayValue);

            for (Long technicianId : request.getTechnicianIds()) {
                StoreScheduleDO schedule = new StoreScheduleDO();
                schedule.setTechnicianId(technicianId);
                schedule.setScheduleDate(current);
                if (isRestDay) {
                    schedule.setScheduleType(2);
                } else {
                    schedule.setScheduleType(request.getScheduleType());
                    schedule.setStartTime(request.getStartTime());
                    schedule.setEndTime(request.getEndTime());
                }
                schedules.add(schedule);
            }
            current = current.plusDays(1);
        }

        for (StoreScheduleDO schedule : schedules) {
            scheduleMapper.insert(schedule);
        }
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, ScheduleUpdateRequest request) {
        StoreScheduleDO schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "排班记录不存在，ID: " + id);
        }
        if (request.getScheduleDate() != null) {
            schedule.setScheduleDate(request.getScheduleDate());
        }
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        if (request.getScheduleType() != null) {
            schedule.setScheduleType(request.getScheduleType());
        }
        if (request.getNotes() != null) {
            schedule.setNotes(request.getNotes());
        }
        scheduleMapper.updateById(schedule);
        return R.ok();
    }

    @Override
    public R<Void> delete(Long id) {
        StoreScheduleDO schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "排班记录不存在，ID: " + id);
        }
        scheduleMapper.deleteById(id);
        return R.ok();
    }
}
