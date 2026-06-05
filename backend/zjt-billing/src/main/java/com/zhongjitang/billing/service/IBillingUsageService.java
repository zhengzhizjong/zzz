package com.zhongjitang.billing.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.billing.domain.entity.BillingUsageDO;
import com.zhongjitang.billing.domain.vo.UsageSummaryVO;

import java.time.LocalDate;
import java.util.List;

public interface IBillingUsageService {

    R<Void> recordUsage(Long tenantId, String metricType, Integer value);

    R<List<BillingUsageDO>> getUsage(Long tenantId, LocalDate startDate, LocalDate endDate);

    R<UsageSummaryVO> getSummary(Long tenantId, LocalDate month);
}
