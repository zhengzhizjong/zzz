package com.zhongjitang.store.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.ScheduleBatchCreateRequest;
import com.zhongjitang.store.domain.dto.ScheduleCreateRequest;
import com.zhongjitang.store.domain.dto.ScheduleUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreScheduleDO;
import com.zhongjitang.store.mapper.StoreScheduleMapper;
import com.zhongjitang.store.service.impl.ScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    @Mock
    private StoreScheduleMapper scheduleMapper;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private StoreScheduleDO mockSchedule;

    @BeforeEach
    void setUp() {
        mockSchedule = new StoreScheduleDO();
        mockSchedule.setId(1L);
        mockSchedule.setTechnicianId(1L);
        mockSchedule.setScheduleDate(LocalDate.of(2026, 6, 5));
        mockSchedule.setStartTime(LocalTime.of(9, 0));
        mockSchedule.setEndTime(LocalTime.of(18, 0));
        mockSchedule.setScheduleType(1);
        mockSchedule.setStoreId(1L);
    }

    @Test
    void testCreateSchedule() {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setTechnicianId(1L);
        request.setScheduleDate(LocalDate.of(2026, 6, 5));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setScheduleType(1);

        when(scheduleMapper.insert(any(StoreScheduleDO.class))).thenReturn(1);

        R<Void> result = scheduleService.create(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(scheduleMapper, times(1)).insert(any(StoreScheduleDO.class));
    }

    @Test
    void testBatchCreate() {
        ScheduleBatchCreateRequest request = new ScheduleBatchCreateRequest();
        request.setTechnicianIds(Arrays.asList(1L, 2L));
        request.setStartDate(LocalDate.of(2026, 6, 1));
        request.setEndDate(LocalDate.of(2026, 6, 3));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setScheduleType(1);
        request.setRestDays(Arrays.asList(0));

        when(scheduleMapper.insert(any(StoreScheduleDO.class))).thenReturn(1);

        R<Void> result = scheduleService.batchCreate(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(scheduleMapper, atLeastOnce()).insert(any(StoreScheduleDO.class));
    }

    @Test
    void testGetByTechnicianAndMonth() {
        when(scheduleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(mockSchedule));

        R<List<StoreScheduleDO>> result = scheduleService.getByTechnicianAndMonth(1L, "2026-06");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void testGetByStoreAndDate() {
        when(scheduleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(mockSchedule));

        R<List<StoreScheduleDO>> result = scheduleService.getByStoreAndDate(1L, LocalDate.of(2026, 6, 5));

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void testUpdateSchedule() {
        ScheduleUpdateRequest request = new ScheduleUpdateRequest();
        request.setNotes("更新备注");

        when(scheduleMapper.selectById(1L)).thenReturn(mockSchedule);
        when(scheduleMapper.updateById(any(StoreScheduleDO.class))).thenReturn(1);

        R<Void> result = scheduleService.update(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(scheduleMapper, times(1)).updateById(any(StoreScheduleDO.class));
    }

    @Test
    void testUpdateNotFound() {
        when(scheduleMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            ScheduleUpdateRequest request = new ScheduleUpdateRequest();
            request.setNotes("测试");
            scheduleService.update(999L, request);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testDeleteSchedule() {
        when(scheduleMapper.selectById(1L)).thenReturn(mockSchedule);
        when(scheduleMapper.deleteById(1L)).thenReturn(1);

        R<Void> result = scheduleService.delete(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(scheduleMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNotFound() {
        when(scheduleMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scheduleService.delete(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }
}
