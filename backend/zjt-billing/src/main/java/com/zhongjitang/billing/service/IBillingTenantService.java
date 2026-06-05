package com.zhongjitang.billing.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.billing.domain.dto.TenantCreateRequest;
import com.zhongjitang.billing.domain.entity.BillingTenantDO;
import com.zhongjitang.billing.domain.vo.TenantVO;

public interface IBillingTenantService {

    R<PageResult<BillingTenantDO>> page(Integer page, Integer pageSize, String keyword, Integer status);

    R<TenantVO> getById(Long id);

    R<Void> create(TenantCreateRequest request);

    R<Void> update(Long id, TenantCreateRequest request);

    R<Void> updateStatus(Long id, Integer status);

    R<Void> updateWhiteLabel(Long id, String logoUrl, String primaryColor, String customDomain);
}
