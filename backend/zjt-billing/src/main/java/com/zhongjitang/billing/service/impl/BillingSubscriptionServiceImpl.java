package com.zhongjitang.billing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.billing.domain.entity.BillingPlanDO;
import com.zhongjitang.billing.domain.entity.BillingSubscriptionDO;
import com.zhongjitang.billing.domain.entity.BillingTenantDO;
import com.zhongjitang.billing.mapper.BillingPlanMapper;
import com.zhongjitang.billing.mapper.BillingSubscriptionMapper;
import com.zhongjitang.billing.mapper.BillingTenantMapper;
import com.zhongjitang.billing.service.IBillingSubscriptionService;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BillingSubscriptionServiceImpl implements IBillingSubscriptionService {

    private final BillingSubscriptionMapper billingSubscriptionMapper;
    private final BillingTenantMapper billingTenantMapper;
    private final BillingPlanMapper billingPlanMapper;

    @Override
    public R<BillingSubscriptionDO> getByTenantId(Long tenantId) {
        LambdaQueryWrapper<BillingSubscriptionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingSubscriptionDO::getTenantId, tenantId);
        wrapper.orderByDesc(BillingSubscriptionDO::getCreatedAt);
        wrapper.last("LIMIT 1");
        BillingSubscriptionDO subscription = billingSubscriptionMapper.selectOne(wrapper);
        if (subscription == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "租户订阅不存在, 租户ID: " + tenantId);
        }
        return R.ok(subscription);
    }

    @Override
    public R<Void> subscribe(Long tenantId, Long planId) {
        BillingTenantDO tenant = billingTenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "租户ID: " + tenantId);
        }
        BillingPlanDO plan = billingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐ID: " + planId);
        }

        BillingSubscriptionDO subscription = new BillingSubscriptionDO();
        subscription.setTenantId(tenantId);
        subscription.setPlanCode(plan.getPlanCode());
        subscription.setPlanName(plan.getPlanName());
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(LocalDate.now().plusMonths(1));
        subscription.setBillingCycle(1);
        subscription.setAutoRenew(1);
        subscription.setPaymentStatus(0);
        subscription.setStatus("active");
        billingSubscriptionMapper.insert(subscription);

        tenant.setPlanCode(plan.getPlanCode());
        tenant.setPlanExpireDate(subscription.getEndDate());
        tenant.setStatus(2);
        billingTenantMapper.updateById(tenant);

        return R.ok();
    }

    @Override
    public R<Void> renew(Long tenantId) {
        LambdaQueryWrapper<BillingSubscriptionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingSubscriptionDO::getTenantId, tenantId);
        wrapper.eq(BillingSubscriptionDO::getStatus, "active");
        wrapper.orderByDesc(BillingSubscriptionDO::getCreatedAt);
        wrapper.last("LIMIT 1");
        BillingSubscriptionDO subscription = billingSubscriptionMapper.selectOne(wrapper);
        if (subscription == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "租户无活跃订阅, 租户ID: " + tenantId);
        }

        LocalDate newEndDate = subscription.getEndDate().plusMonths(1);
        subscription.setEndDate(newEndDate);
        subscription.setPaymentStatus(1);
        billingSubscriptionMapper.updateById(subscription);

        BillingTenantDO tenant = billingTenantMapper.selectById(tenantId);
        if (tenant != null) {
            tenant.setPlanExpireDate(newEndDate);
            tenant.setStatus(2);
            billingTenantMapper.updateById(tenant);
        }

        return R.ok();
    }

    @Override
    public R<Void> cancel(Long tenantId) {
        LambdaQueryWrapper<BillingSubscriptionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingSubscriptionDO::getTenantId, tenantId);
        wrapper.eq(BillingSubscriptionDO::getStatus, "active");
        wrapper.orderByDesc(BillingSubscriptionDO::getCreatedAt);
        wrapper.last("LIMIT 1");
        BillingSubscriptionDO subscription = billingSubscriptionMapper.selectOne(wrapper);
        if (subscription == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "租户无活跃订阅, 租户ID: " + tenantId);
        }

        subscription.setStatus("cancelled");
        subscription.setAutoRenew(0);
        billingSubscriptionMapper.updateById(subscription);

        return R.ok();
    }
}
