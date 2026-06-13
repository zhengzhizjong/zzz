package com.zhongjitang.system.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.entity.SysConfigDO;

import java.util.List;

public interface ISysConfigService {

    R<PageResult<SysConfigDO>> page(Integer page, Integer pageSize, String keyword);

    R<SysConfigDO> getById(Long id);

    R<List<SysConfigDO>> list();

    R<SysConfigDO> getByKey(String key);

    R<Void> update(String key, String value);

    R<Void> updateById(Long id, String configValue);
}
