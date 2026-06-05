package com.zhongjitang.integration.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.integration.domain.dto.LeadCreateRequest;
import com.zhongjitang.integration.domain.dto.LeadUpdateRequest;
import com.zhongjitang.integration.domain.entity.IntegrationLeadDO;
import com.zhongjitang.integration.mapper.IntegrationLeadMapper;
import com.zhongjitang.integration.service.impl.LeadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadServiceImplTest {

    @Mock
    private IntegrationLeadMapper integrationLeadMapper;

    @InjectMocks
    private LeadServiceImpl leadService;

    private IntegrationLeadDO mockLead;

    @BeforeEach
    void setUp() {
        mockLead = new IntegrationLeadDO();
        mockLead.setId(1L);
        mockLead.setLeadNo("L202606050001");
        mockLead.setPlatform("wecom");
        mockLead.setCustomerName("李四");
        mockLead.setCustomerPhone("13900139000");
        mockLead.setFollowStatus(1);
    }

    @Test
    void testCreateLead() {
        LeadCreateRequest request = new LeadCreateRequest();
        request.setPlatform("wecom");
        request.setCustomerName("测试客户");
        request.setCustomerPhone("13900139000");

        when(integrationLeadMapper.insert(any(IntegrationLeadDO.class))).thenReturn(1);

        R<Void> result = leadService.create(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(integrationLeadMapper, times(1)).insert(any(IntegrationLeadDO.class));
    }

    @Test
    void testPageQuery() {
        Page<IntegrationLeadDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(mockLead));
        pageResult.setTotal(1);

        when(integrationLeadMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        R<PageResult<IntegrationLeadDO>> result = leadService.page(1, 20, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
    }

    @Test
    void testGetById() {
        when(integrationLeadMapper.selectById(1L)).thenReturn(mockLead);

        R<IntegrationLeadDO> result = leadService.getById(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("李四", result.getData().getCustomerName());
    }

    @Test
    void testGetByIdNotFound() {
        when(integrationLeadMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            leadService.getById(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testUpdateLead() {
        LeadUpdateRequest request = new LeadUpdateRequest();
        request.setCustomerName("更新后客户名");

        when(integrationLeadMapper.selectById(1L)).thenReturn(mockLead);
        when(integrationLeadMapper.updateById(any(IntegrationLeadDO.class))).thenReturn(1);

        R<Void> result = leadService.update(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(integrationLeadMapper, times(1)).updateById(any(IntegrationLeadDO.class));
    }

    @Test
    void testAssign() {
        when(integrationLeadMapper.selectById(1L)).thenReturn(mockLead);
        when(integrationLeadMapper.updateById(any(IntegrationLeadDO.class))).thenReturn(1);

        R<Void> result = leadService.assign(1L, 100L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(integrationLeadMapper, times(1)).updateById(any(IntegrationLeadDO.class));
    }

    @Test
    void testConvert() {
        when(integrationLeadMapper.selectById(1L)).thenReturn(mockLead);
        when(integrationLeadMapper.updateById(any(IntegrationLeadDO.class))).thenReturn(1);

        R<Void> result = leadService.convert(1L, 200L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(integrationLeadMapper, times(1)).updateById(any(IntegrationLeadDO.class));
    }

    @Test
    void testUpdateStatus() {
        when(integrationLeadMapper.selectById(1L)).thenReturn(mockLead);
        when(integrationLeadMapper.updateById(any(IntegrationLeadDO.class))).thenReturn(1);

        R<Void> result = leadService.updateStatus(1L, 2);

        assertNotNull(result);
        assertEquals(0, result.getCode());
    }

    @Test
    void testUpdateStatusInvalid() {
        when(integrationLeadMapper.selectById(1L)).thenReturn(mockLead);

        R<Void> result = leadService.updateStatus(1L, 5);

        assertNotNull(result);
        assertNotEquals(0, result.getCode());
    }
}
