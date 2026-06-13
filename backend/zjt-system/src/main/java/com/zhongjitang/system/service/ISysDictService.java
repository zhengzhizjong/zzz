package com.zhongjitang.system.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.SysDictCreateRequest;
import com.zhongjitang.system.domain.dto.SysDictItemCreateRequest;
import com.zhongjitang.system.domain.dto.SysDictItemUpdateRequest;
import com.zhongjitang.system.domain.dto.SysDictUpdateRequest;
import com.zhongjitang.system.domain.entity.SysDictDO;
import com.zhongjitang.system.domain.entity.SysDictItemDO;

import java.util.List;

public interface ISysDictService {

    R<PageResult<SysDictDO>> page(Integer page, Integer pageSize, String keyword);

    R<SysDictDO> getById(Long id);

    R<Void> create(SysDictCreateRequest request);

    R<Void> update(Long id, SysDictUpdateRequest request);

    R<Void> delete(Long id);

    R<List<SysDictItemDO>> getItems(Long dictId);

    R<Void> addItem(SysDictItemCreateRequest request);

    R<Void> updateItem(Long id, SysDictItemUpdateRequest request);

    R<Void> deleteItem(Long id);

    R<List<SysDictItemDO>> getItemsByCode(String dictCode);
}
