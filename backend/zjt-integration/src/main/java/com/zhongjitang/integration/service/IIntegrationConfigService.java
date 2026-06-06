package com.zhongjitang.integration.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.integration.domain.dto.ConfigCreateRequest;
import com.zhongjitang.integration.domain.dto.ConfigUpdateRequest;
import com.zhongjitang.integration.domain.entity.IntegrationConfigDO;

import java.util.List;

public interface IIntegrationConfigService {

    R<List<IntegrationConfigDO>> list();

    R<IntegrationConfigDO> getByPlatform(String platform);

    R<Void> create(ConfigCreateRequest request);

    R<Void> update(Long id, ConfigUpdateRequest request);

    R<Void> updateStatus(Long id, Integer status);
}
