package com.zhongjitang.data.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.data.domain.dto.FunnelTrackRequest;
import com.zhongjitang.data.domain.entity.DataAppointmentFunnelDO;
import com.zhongjitang.data.domain.vo.FunnelStatisticsVO;
import com.zhongjitang.data.mapper.DataAppointmentFunnelMapper;
import com.zhongjitang.data.service.impl.AppointmentFunnelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentFunnelServiceTest {

    @Mock
    private DataAppointmentFunnelMapper funnelMapper;

    @InjectMocks
    private AppointmentFunnelServiceImpl appointmentFunnelService;

    private FunnelTrackRequest trackRequest;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        trackRequest = new FunnelTrackRequest();
        trackRequest.setStoreId(1L);
        trackRequest.setSessionId("sess_test123");
        trackRequest.setStep("browse_store");
        trackRequest.setMemberId(1001L);

        startDate = LocalDate.of(2026, 6, 1);
        endDate = LocalDate.of(2026, 6, 4);
    }

    @Test
    void testTrack() {
        when(funnelMapper.insert(any(DataAppointmentFunnelDO.class))).thenReturn(1);

        R<Void> result = appointmentFunnelService.track(trackRequest);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(funnelMapper, times(1)).insert(any(DataAppointmentFunnelDO.class));
    }

    @Test
    void testTrackWithAllFields() {
        trackRequest.setStep("confirm");
        trackRequest.setTechnicianId(10L);
        trackRequest.setDate(LocalDate.of(2026, 6, 5));
        trackRequest.setTimeSlot("10:00-11:00");
        trackRequest.setExtraJson("{\"channel\":\"wechat\"}");

        when(funnelMapper.insert(any(DataAppointmentFunnelDO.class))).thenReturn(1);

        R<Void> result = appointmentFunnelService.track(trackRequest);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(funnelMapper, times(1)).insert(any(DataAppointmentFunnelDO.class));
    }

    @Test
    void testGetStatistics() {
        List<DataAppointmentFunnelDO> records = buildFunnelRecords();
        when(funnelMapper.selectList(any())).thenReturn(records);

        R<FunnelStatisticsVO> result = appointmentFunnelService.getStatistics(1L, startDate, endDate, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        FunnelStatisticsVO vo = result.getData();
        assertNotNull(vo);

        // 验证周期
        assertNotNull(vo.getPeriod());
        assertEquals(startDate, vo.getPeriod().getStartDate());
        assertEquals(endDate, vo.getPeriod().getEndDate());

        // 验证漏斗数据
        Map<String, FunnelStatisticsVO.FunnelStep> funnel = vo.getFunnel();
        assertNotNull(funnel);
        assertEquals(6, funnel.size());

        // 验证第一步数量
        FunnelStatisticsVO.FunnelStep browseStep = funnel.get("browse_store");
        assertEquals(100L, browseStep.getCount());
        assertEquals(100.0, browseStep.getRate());

        // 验证最后一步
        FunnelStatisticsVO.FunnelStep completeStep = funnel.get("complete");
        assertEquals(20L, completeStep.getCount());
        assertEquals(20.0, completeStep.getRate());
    }

    @Test
    void testGetStatisticsConversionRate() {
        List<DataAppointmentFunnelDO> records = buildFunnelRecords();
        when(funnelMapper.selectList(any())).thenReturn(records);

        R<FunnelStatisticsVO> result = appointmentFunnelService.getStatistics(1L, startDate, endDate, null);

        FunnelStatisticsVO vo = result.getData();
        Map<String, Double> conversion = vo.getConversion();
        assertNotNull(conversion);

        // 验证相邻步骤转化率
        // browse_store(100) -> select_tech(80): 80%
        assertEquals(80.0, conversion.get("browse_store_to_select_tech"));
        // select_tech(80) -> select_time(60): 75%
        assertEquals(75.0, conversion.get("select_tech_to_select_time"));
        // select_time(60) -> confirm(40): 66.67%
        assertEquals(66.67, conversion.get("select_time_to_confirm"), 0.01);
        // confirm(40) -> arrive(30): 75%
        assertEquals(75.0, conversion.get("confirm_to_arrive"));
        // arrive(30) -> complete(20): 66.67%
        assertEquals(66.67, conversion.get("arrive_to_complete"), 0.01);

        // 验证整体转化率: complete/browse_store = 20/100 = 20%
        assertEquals(20.0, conversion.get("overall"));
    }

    @Test
    void testGetStatisticsWithGroupByStore() {
        List<DataAppointmentFunnelDO> records = buildFunnelRecords();
        when(funnelMapper.selectList(any())).thenReturn(records);

        R<FunnelStatisticsVO> result = appointmentFunnelService.getStatistics(1L, startDate, endDate, "store");

        FunnelStatisticsVO vo = result.getData();
        assertNotNull(vo.getGroups());
        assertFalse(vo.getGroups().isEmpty());

        FunnelStatisticsVO.FunnelGroup group = vo.getGroups().get(0);
        assertEquals("store", group.getDimension());
        assertNotNull(group.getBrowseStoreCount());
    }

    @Test
    void testGetStatisticsWithGroupByDate() {
        List<DataAppointmentFunnelDO> records = buildFunnelRecords();
        when(funnelMapper.selectList(any())).thenReturn(records);

        R<FunnelStatisticsVO> result = appointmentFunnelService.getStatistics(1L, startDate, endDate, "date");

        FunnelStatisticsVO vo = result.getData();
        assertNotNull(vo.getGroups());
        assertFalse(vo.getGroups().isEmpty());

        FunnelStatisticsVO.FunnelGroup group = vo.getGroups().get(0);
        assertEquals("date", group.getDimension());
    }

    @Test
    void testGetStatisticsEmptyData() {
        when(funnelMapper.selectList(any())).thenReturn(Collections.emptyList());

        R<FunnelStatisticsVO> result = appointmentFunnelService.getStatistics(1L, startDate, endDate, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        FunnelStatisticsVO vo = result.getData();
        assertNotNull(vo.getFunnel());

        // 所有步骤数量应为0
        for (String step : Arrays.asList("browse_store", "select_tech", "select_time", "confirm", "arrive", "complete")) {
            FunnelStatisticsVO.FunnelStep funnelStep = vo.getFunnel().get(step);
            assertEquals(0L, funnelStep.getCount());
            assertEquals(0.0, funnelStep.getRate());
        }
    }

    @Test
    void testGetStatisticsWithoutStoreId() {
        List<DataAppointmentFunnelDO> records = buildFunnelRecords();
        when(funnelMapper.selectList(any())).thenReturn(records);

        R<FunnelStatisticsVO> result = appointmentFunnelService.getStatistics(null, startDate, endDate, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
    }

    private List<DataAppointmentFunnelDO> buildFunnelRecords() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 6, 1, 10, 0);
        return Arrays.asList(
                createFunnelDO(1L, 1L, "sess_1", "browse_store", baseTime),
                createFunnelDO(2L, 1L, "sess_1", "select_tech", baseTime.plusMinutes(5)),
                createFunnelDO(3L, 1L, "sess_1", "select_time", baseTime.plusMinutes(10)),
                createFunnelDO(4L, 1L, "sess_1", "confirm", baseTime.plusMinutes(15)),
                createFunnelDO(5L, 1L, "sess_1", "arrive", baseTime.plusMinutes(20)),
                createFunnelDO(6L, 1L, "sess_1", "complete", baseTime.plusMinutes(60))
        );
    }

    private DataAppointmentFunnelDO createFunnelDO(Long id, Long storeId, String sessionId, String step, LocalDateTime stepTime) {
        DataAppointmentFunnelDO funnelDO = new DataAppointmentFunnelDO();
        funnelDO.setId(id);
        funnelDO.setStoreId(storeId);
        funnelDO.setSessionId(sessionId);
        funnelDO.setStep(step);
        funnelDO.setStepTime(stepTime);
        funnelDO.setMemberId(1001L);
        return funnelDO;
    }
}
