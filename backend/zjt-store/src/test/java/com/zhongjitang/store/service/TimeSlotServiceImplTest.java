package com.zhongjitang.store.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.entity.StoreTimeSlotConfigDO;
import com.zhongjitang.store.domain.vo.TimeSlotVO;
import com.zhongjitang.store.mapper.StoreTimeSlotConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceImplTest {

    @Mock
    private StoreTimeSlotConfigMapper timeSlotConfigMapper;

    @InjectMocks
    private TimeSlotServiceImpl timeSlotService;

    private StoreTimeSlotConfigDO mockConfig;

    @BeforeEach
    void setUp() {
        mockConfig = new StoreTimeSlotConfigDO();
        mockConfig.setId(1L);
        mockConfig.setStoreId(1L);
        mockConfig.setBusinessStartTime(LocalTime.of(9, 0));
        mockConfig.setBusinessEndTime(LocalTime.of(21, 0));
        mockConfig.setSlotDurationMinutes(60);
        mockConfig.setStatus(1);
    }

    @Test
    void testGenerateTimeSlots() {
        when(timeSlotConfigMapper.selectOne(any())).thenReturn(mockConfig);

        R<List<TimeSlotVO>> result = timeSlotService.getAvailableSlots(1L, 0L, LocalDate.now());

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        // 09:00-21:00 = 12 hours, 12 slots with 60min duration
        assertEquals(12, result.getData().size());
        assertEquals("09:00", result.getData().get(0).getTimeSlot());
        assertEquals("20:00", result.getData().get(11).getTimeSlot());
    }

    @Test
    void testExcludeRestPeriod() {
        mockConfig.setRestStartTime(LocalTime.of(12, 0));
        mockConfig.setRestEndTime(LocalTime.of(13, 0));

        when(timeSlotConfigMapper.selectOne(any())).thenReturn(mockConfig);

        R<List<TimeSlotVO>> result = timeSlotService.getAvailableSlots(1L, 0L, LocalDate.now());

        assertNotNull(result);
        assertEquals(0, result.getCode());
        List<TimeSlotVO> slots = result.getData();
        // 12 slots minus 1 (12:00-13:00 rest) = 11
        assertEquals(11, slots.size());
        // Verify 12:00 slot is excluded
        boolean hasNoonSlot = slots.stream().anyMatch(s -> "12:00".equals(s.getTimeSlot()));
        assertFalse(hasNoonSlot, "12:00时段应被休息时段排除");
    }

    @Test
    void testAvailableSlotsWith30MinDuration() {
        mockConfig.setSlotDurationMinutes(30);

        when(timeSlotConfigMapper.selectOne(any())).thenReturn(mockConfig);

        R<List<TimeSlotVO>> result = timeSlotService.getAvailableSlots(1L, 0L, LocalDate.now());

        assertNotNull(result);
        assertEquals(0, result.getCode());
        // 09:00-21:00 = 12 hours, 24 slots with 30min duration
        assertEquals(24, result.getData().size());
    }

    @Test
    void testRestPeriodBoundary() {
        mockConfig.setBusinessStartTime(LocalTime.of(9, 0));
        mockConfig.setBusinessEndTime(LocalTime.of(18, 0));
        mockConfig.setSlotDurationMinutes(60);
        mockConfig.setRestStartTime(LocalTime.of(12, 0));
        mockConfig.setRestEndTime(LocalTime.of(14, 0));

        when(timeSlotConfigMapper.selectOne(any())).thenReturn(mockConfig);

        R<List<TimeSlotVO>> result = timeSlotService.getAvailableSlots(1L, 0L, LocalDate.now());

        assertNotNull(result);
        List<TimeSlotVO> slots = result.getData();
        // 09:00-18:00 = 9 hours = 9 slots, minus 12:00 and 13:00 = 7
        assertEquals(7, slots.size());
        // Verify excluded slots
        boolean has12 = slots.stream().anyMatch(s -> "12:00".equals(s.getTimeSlot()));
        boolean has13 = slots.stream().anyMatch(s -> "13:00".equals(s.getTimeSlot()));
        assertFalse(has12, "12:00时段应被休息时段排除");
        assertFalse(has13, "13:00时段应被休息时段排除");
    }
}
