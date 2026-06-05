package com.zhongjitang.ai.service;

import com.zhongjitang.ai.domain.dto.LlmRequest;
import com.zhongjitang.ai.domain.entity.AiDailyReportDO;
import com.zhongjitang.ai.domain.vo.LlmResponse;
import com.zhongjitang.ai.mapper.AiDailyReportMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiDailyReportServiceTest {

    @Mock
    private LlmGatewayService llmGatewayService;

    @Mock
    private AiDailyReportMapper dailyReportMapper;

    @InjectMocks
    private AiDailyReportService aiDailyReportService;

    private LlmResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new LlmResponse();
        mockResponse.setContent("{\"summary\":\"门店运营良好，完成率84.4%，营收15800元\"," +
                "\"suggestions\":\"建议增加下午时段技师排班，降低取消率\"}");
        mockResponse.setModel("deepseek-chat");
        mockResponse.setProvider("deepseek");
    }

    @Test
    void testGenerateDailyReport() {
        when(llmGatewayService.chat(any(LlmRequest.class))).thenReturn(mockResponse);
        when(dailyReportMapper.insert(any(AiDailyReportDO.class))).thenReturn(1);

        AiDailyReportDO report = aiDailyReportService.generateDailyReport(1L, LocalDate.now());

        assertNotNull(report);
        assertNotNull(report.getSummary());
        assertEquals(LocalDate.now(), report.getReportDate());
        assertEquals(1L, report.getStoreId());
        verify(dailyReportMapper, times(1)).insert(any(AiDailyReportDO.class));
    }

    @Test
    void testGetReport() {
        AiDailyReportDO mockReport = new AiDailyReportDO();
        mockReport.setId(1L);
        mockReport.setStoreId(1L);
        mockReport.setReportDate(LocalDate.now());
        mockReport.setSummary("测试日报");

        when(dailyReportMapper.selectOne(any())).thenReturn(mockReport);

        AiDailyReportDO report = aiDailyReportService.getReport(1L, LocalDate.now());

        assertNotNull(report);
        assertEquals("测试日报", report.getSummary());
    }

    @Test
    void testListReports() {
        when(dailyReportMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());

        var reports = aiDailyReportService.listReports(1L, LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(reports);
        verify(dailyReportMapper, times(1)).selectList(any());
    }
}
