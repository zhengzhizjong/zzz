package com.zhongjitang.system.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.FeatureFlagCreateRequest;
import com.zhongjitang.system.domain.dto.FeatureFlagUpdateRequest;
import com.zhongjitang.system.domain.entity.SysFeatureFlagDO;

import java.util.List;

public interface ISysFeatureFlagService {

    R<PageResult<SysFeatureFlagDO>> page(Integer page, Integer pageSize, String keyword);

    R<SysFeatureFlagDO> getById(Long id);

    R<List<SysFeatureFlagDO>> list();

    R<SysFeatureFlagDO> getByKey(String key);

    R<Void> create(FeatureFlagCreateRequest request);

    R<Void> update(String key, FeatureFlagUpdateRequest request);

    R<Void> updateById(Long id, FeatureFlagUpdateRequest request);

    R<Void> toggle(Long id, Boolean enabled);

    R<Void> delete(Long id);

    R<Boolean> isEnabled(String key, Long tenantId);
}
