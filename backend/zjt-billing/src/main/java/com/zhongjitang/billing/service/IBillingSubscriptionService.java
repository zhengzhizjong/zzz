package com.zhongjitang.billing.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.billing.domain.entity.BillingSubscriptionDO;

public interface IBillingSubscriptionService {

    R<BillingSubscriptionDO> getByTenantId(Long tenantId);

    R<Void> subscribe(Long tenantId, Long planId);

    R<Void> renew(Long tenantId);

    R<Void> cancel(Long tenantId);
}
