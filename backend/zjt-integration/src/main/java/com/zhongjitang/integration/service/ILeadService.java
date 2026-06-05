package com.zhongjitang.integration.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.integration.domain.dto.LeadCreateRequest;
import com.zhongjitang.integration.domain.dto.LeadUpdateRequest;
import com.zhongjitang.integration.domain.entity.IntegrationLeadDO;

public interface ILeadService {

    R<PageResult<IntegrationLeadDO>> page(Integer page, Integer pageSize, String keyword, String platform, Integer status);

    R<IntegrationLeadDO> getById(Long id);

    R<Void> create(LeadCreateRequest request);

    R<Void> update(Long id, LeadUpdateRequest request);

    R<Void> assign(Long id, Long assignedTo);

    R<Void> convert(Long id, Long memberId);

    R<Void> updateStatus(Long id, Integer status);
}
