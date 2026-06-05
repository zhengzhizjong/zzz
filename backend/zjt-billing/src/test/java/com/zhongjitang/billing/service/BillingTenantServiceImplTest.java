package com.zhongjitang.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.billing.domain.dto.TenantCreateRequest;
import com.zhongjitang.billing.domain.entity.BillingPlanDO;
import com.zhongjitang.billing.domain.entity.BillingTenantDO;
import com.zhongjitang.billing.domain.vo.TenantVO;
import com.zhongjitang.billing.mapper.BillingPlanMapper;
import com.zhongjitang.billing.mapper.BillingTenantMapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingTenantServiceImplTest {

    @Mock
    private BillingTenantMapper billingTenantMapper;

    @Mock
    private BillingPlanMapper billingPlanMapper;

    @InjectMocks
    private BillingTenantServiceImpl billingTenantService;

    private BillingTenantDO mockTenant;

    @BeforeEach
    void setUp() {
        mockTenant = new BillingTenantDO();
        mockTenant.setId(1L);
        mockTenant.setTenantNo("T202606050001");
        mockTenant.setTenantName("忠济堂国医馆");
        mockTenant.setContactName("张三");
        mockTenant.setContactPhone("13800138000");
        mockTenant.setPlanCode("basic");
        mockTenant.setStatus(1);
    }

    @Test
    void testCreateTenant() {
        TenantCreateRequest request = new TenantCreateRequest();
        request.setTenantName("测试租户");
        request.setContactName("张三");
        request.setContactPhone("13800138000");
        request.setPlanCode("basic");

        when(billingTenantMapper.insert(any(BillingTenantDO.class))).thenReturn(1);

        R<Void> result = billingTenantService.create(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(billingTenantMapper, times(1)).insert(any(BillingTenantDO.class));
    }

    @Test
    void testPageQuery() {
        Page<BillingTenantDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(mockTenant));
        pageResult.setTotal(1);

        when(billingTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        R<PageResult<BillingTenantDO>> result = billingTenantService.page(1, 20, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
    }

    @Test
    void testGetById() {
        when(billingTenantMapper.selectById(1L)).thenReturn(mockTenant);

        R<TenantVO> result = billingTenantService.getById(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("忠济堂国医馆", result.getData().getTenantName());
        assertEquals("试用", result.getData().getStatusName());
    }

    @Test
    void testGetByIdNotFound() {
        when(billingTenantMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            billingTenantService.getById(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testUpdateTenant() {
        TenantCreateRequest request = new TenantCreateRequest();
        request.setTenantName("更新后租户名");

        when(billingTenantMapper.selectById(1L)).thenReturn(mockTenant);
        when(billingTenantMapper.updateById(any(BillingTenantDO.class))).thenReturn(1);

        R<Void> result = billingTenantService.update(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(billingTenantMapper, times(1)).updateById(any(BillingTenantDO.class));
    }

    @Test
    void testUpdateStatus() {
        when(billingTenantMapper.selectById(1L)).thenReturn(mockTenant);
        when(billingTenantMapper.updateById(any(BillingTenantDO.class))).thenReturn(1);

        R<Void> result = billingTenantService.updateStatus(1L, 2);

        assertNotNull(result);
        assertEquals(0, result.getCode());
    }

    @Test
    void testUpdateStatusInvalid() {
        when(billingTenantMapper.selectById(1L)).thenReturn(mockTenant);

        R<Void> result = billingTenantService.updateStatus(1L, 5);

        assertNotNull(result);
        assertNotEquals(0, result.getCode());
    }

    @Test
    void testUpdateWhiteLabel() {
        when(billingTenantMapper.selectById(1L)).thenReturn(mockTenant);
        when(billingTenantMapper.updateById(any(BillingTenantDO.class))).thenReturn(1);

        R<Void> result = billingTenantService.updateWhiteLabel(1L, "https://logo.png", "#FF0000", "custom.zhongjitang.com");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(billingTenantMapper, times(1)).updateById(any(BillingTenantDO.class));
    }
}
