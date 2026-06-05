package com.zhongjitang.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.billing.domain.entity.BillingPlanDO;
import com.zhongjitang.billing.domain.entity.BillingSubscriptionDO;
import com.zhongjitang.billing.domain.entity.BillingTenantDO;
import com.zhongjitang.billing.mapper.BillingPlanMapper;
import com.zhongjitang.billing.mapper.BillingSubscriptionMapper;
import com.zhongjitang.billing.mapper.BillingTenantMapper;
import com.zhongjitang.billing.service.impl.BillingSubscriptionServiceImpl;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingSubscriptionServiceImplTest {

    @Mock
    private BillingSubscriptionMapper billingSubscriptionMapper;

    @Mock
    private BillingTenantMapper billingTenantMapper;

    @Mock
    private BillingPlanMapper billingPlanMapper;

    @InjectMocks
    private BillingSubscriptionServiceImpl billingSubscriptionService;

    private BillingTenantDO mockTenant;
    private BillingPlanDO mockPlan;
    private BillingSubscriptionDO mockSubscription;

    @BeforeEach
    void setUp() {
        mockTenant = new BillingTenantDO();
        mockTenant.setId(1L);
        mockTenant.setTenantNo("T202606050001");
        mockTenant.setTenantName("忠济堂国医馆");
        mockTenant.setPlanCode("basic");
        mockTenant.setStatus(1);

        mockPlan = new BillingPlanDO();
        mockPlan.setId(1L);
        mockPlan.setPlanCode("basic");
        mockPlan.setPlanName("基础版");
        mockPlan.setMonthlyPrice(new BigDecimal("299.00"));

        mockSubscription = new BillingSubscriptionDO();
        mockSubscription.setId(1L);
        mockSubscription.setTenantId(1L);
        mockSubscription.setPlanCode("basic");
        mockSubscription.setPlanName("基础版");
        mockSubscription.setStartDate(LocalDate.now());
        mockSubscription.setEndDate(LocalDate.now().plusMonths(1));
        mockSubscription.setStatus("active");
        mockSubscription.setAutoRenew(1);
    }

    @Test
    void testGetByTenantId() {
        when(billingSubscriptionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockSubscription);

        R<BillingSubscriptionDO> result = billingSubscriptionService.getByTenantId(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("basic", result.getData().getPlanCode());
    }

    @Test
    void testGetByTenantIdNotFound() {
        when(billingSubscriptionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            billingSubscriptionService.getByTenantId(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testSubscribe() {
        when(billingTenantMapper.selectById(1L)).thenReturn(mockTenant);
        when(billingPlanMapper.selectById(1L)).thenReturn(mockPlan);
        when(billingSubscriptionMapper.insert(any(BillingSubscriptionDO.class))).thenReturn(1);
        when(billingTenantMapper.updateById(any(BillingTenantDO.class))).thenReturn(1);

        R<Void> result = billingSubscriptionService.subscribe(1L, 1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(billingSubscriptionMapper, times(1)).insert(any(BillingSubscriptionDO.class));
        verify(billingTenantMapper, times(1)).updateById(any(BillingTenantDO.class));
    }

    @Test
    void testSubscribeTenantNotFound() {
        when(billingTenantMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            billingSubscriptionService.subscribe(999L, 1L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testSubscribePlanNotFound() {
        when(billingTenantMapper.selectById(1L)).thenReturn(mockTenant);
        when(billingPlanMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            billingSubscriptionService.subscribe(1L, 999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testRenew() {
        when(billingSubscriptionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockSubscription);
        when(billingSubscriptionMapper.updateById(any(BillingSubscriptionDO.class))).thenReturn(1);
        when(billingTenantMapper.selectById(1L)).thenReturn(mockTenant);
        when(billingTenantMapper.updateById(any(BillingTenantDO.class))).thenReturn(1);

        R<Void> result = billingSubscriptionService.renew(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(billingSubscriptionMapper, times(1)).updateById(any(BillingSubscriptionDO.class));
    }

    @Test
    void testRenewNoActiveSubscription() {
        when(billingSubscriptionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            billingSubscriptionService.renew(1L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testCancel() {
        when(billingSubscriptionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockSubscription);
        when(billingSubscriptionMapper.updateById(any(BillingSubscriptionDO.class))).thenReturn(1);

        R<Void> result = billingSubscriptionService.cancel(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(billingSubscriptionMapper, times(1)).updateById(any(BillingSubscriptionDO.class));
    }

    @Test
    void testCancelNoActiveSubscription() {
        when(billingSubscriptionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            billingSubscriptionService.cancel(1L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }
}
