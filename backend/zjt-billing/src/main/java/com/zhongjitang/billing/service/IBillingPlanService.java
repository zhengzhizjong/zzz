package com.zhongjitang.billing.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.billing.domain.dto.BillingPlanCreateRequest;
import com.zhongjitang.billing.domain.entity.BillingPlanDO;

import java.util.List;

public interface IBillingPlanService {

    R<List<BillingPlanDO>> list();

    R<BillingPlanDO> getById(Long id);

    R<Void> create(BillingPlanCreateRequest request);

    R<Void> update(Long id, BillingPlanCreateRequest request);
}
