package com.zhongjitang.billing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.billing.domain.entity.BillingPlanDO;
import com.zhongjitang.billing.domain.entity.BillingUsageDO;
import com.zhongjitang.billing.domain.vo.UsageSummaryVO;
import com.zhongjitang.billing.mapper.BillingPlanMapper;
import com.zhongjitang.billing.mapper.BillingUsageMapper;
import com.zhongjitang.billing.service.IBillingUsageService;
import com.zhongjitang.common.core.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BillingUsageServiceImpl implements IBillingUsageService {

    private final BillingUsageMapper billingUsageMapper;
    private final BillingPlanMapper billingPlanMapper;

    @Override
    public R<Void> recordUsage(Long tenantId, String metricType, Integer value) {
        LocalDate today = LocalDate.now();

        LambdaQueryWrapper<BillingUsageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingUsageDO::getTenantId, tenantId);
        wrapper.eq(BillingUsageDO::getUsageType, metricType);
        wrapper.eq(BillingUsageDO::getUsageDate, today);
        BillingUsageDO existing = billingUsageMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setUsageCount(existing.getUsageCount() + value);
            billingUsageMapper.updateById(existing);
        } else {
            BillingUsageDO usage = new BillingUsageDO();
            usage.setTenantId(tenantId);
            usage.setUsageType(metricType);
            usage.setUsageDate(today);
            usage.setUsageCount(value);
            billingUsageMapper.insert(usage);
        }
        return R.ok();
    }

    @Override
    public R<List<BillingUsageDO>> getUsage(Long tenantId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<BillingUsageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingUsageDO::getTenantId, tenantId);
        if (startDate != null) {
            wrapper.ge(BillingUsageDO::getUsageDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(BillingUsageDO::getUsageDate, endDate);
        }
        wrapper.orderByDesc(BillingUsageDO::getUsageDate);
        List<BillingUsageDO> list = billingUsageMapper.selectList(wrapper);
        return R.ok(list);
    }

    @Override
    public R<UsageSummaryVO> getSummary(Long tenantId, LocalDate month) {
        LocalDate startDate = month.withDayOfMonth(1);
        LocalDate endDate = month.withDayOfMonth(month.lengthOfMonth());

        LambdaQueryWrapper<BillingUsageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingUsageDO::getTenantId, tenantId);
        wrapper.ge(BillingUsageDO::getUsageDate, startDate);
        wrapper.le(BillingUsageDO::getUsageDate, endDate);
        List<BillingUsageDO> usageList = billingUsageMapper.selectList(wrapper);

        Map<String, Integer> metrics = new HashMap<>();
        for (BillingUsageDO usage : usageList) {
            metrics.merge(usage.getUsageType(), usage.getUsageCount(), Integer::sum);
        }

        Map<String, Integer> quotas = new HashMap<>();
        Map<String, Integer> overages = new HashMap<>();
        for (Map.Entry<String, Integer> entry : metrics.entrySet()) {
            int quota = getQuotaForMetric(tenantId, entry.getKey());
            quotas.put(entry.getKey(), quota);
            int overage = Math.max(0, entry.getValue() - quota);
            if (overage > 0) {
                overages.put(entry.getKey(), overage);
            }
        }

        UsageSummaryVO summary = new UsageSummaryVO();
        summary.setTenantId(tenantId);
        summary.setMonth(month.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
        summary.setMetrics(metrics);
        summary.setQuotas(quotas);
        summary.setOverages(overages);
        return R.ok(summary);
    }

    private int getQuotaForMetric(Long tenantId, String metricType) {
        switch (metricType) {
            case "appointment":
                return 500;
            case "ai_call":
                return 100;
            case "store_count":
                return getPlanMaxStores(tenantId);
            case "tech_count":
                return 20;
            default:
                return Integer.MAX_VALUE;
        }
    }

    private int getPlanMaxStores(Long tenantId) {
        try {
            LambdaQueryWrapper<BillingPlanDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BillingPlanDO::getStatus, 1);
            wrapper.last("LIMIT 1");
            BillingPlanDO plan = billingPlanMapper.selectOne(wrapper);
            if (plan != null && plan.getMaxStores() != null) {
                return plan.getMaxStores();
            }
        } catch (Exception ignored) {
        }
        return 3;
    }
}
