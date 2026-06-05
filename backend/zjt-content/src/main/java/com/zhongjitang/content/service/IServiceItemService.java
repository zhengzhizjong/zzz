package com.zhongjitang.content.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.content.domain.dto.ServiceItemCreateRequest;
import com.zhongjitang.content.domain.dto.ServiceItemUpdateRequest;
import com.zhongjitang.content.domain.entity.ContentServiceItemDO;

import java.util.List;

public interface IServiceItemService {

    R<PageResult<ContentServiceItemDO>> page(Integer page, Integer pageSize, String keyword, Integer status, Long categoryId);

    R<ContentServiceItemDO> getById(Long id);

    R<Void> create(ServiceItemCreateRequest request);

    R<Void> update(Long id, ServiceItemUpdateRequest request);

    R<Void> updateStatus(Long id, Integer status);

    R<List<ContentServiceItemDO>> listAll(Integer status);
}
