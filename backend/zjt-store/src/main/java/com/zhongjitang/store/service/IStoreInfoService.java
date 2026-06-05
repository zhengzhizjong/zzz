package com.zhongjitang.store.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.StoreCreateRequest;
import com.zhongjitang.store.domain.dto.StoreUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreInfoDO;

public interface IStoreInfoService {

    R<PageResult<StoreInfoDO>> page(Integer page, Integer pageSize, String keyword, Integer status);

    R<StoreInfoDO> getById(Long id);

    R<Void> create(StoreCreateRequest request);

    R<Void> update(Long id, StoreUpdateRequest request);

    R<Void> updateStatus(Long id, Integer status);
}
