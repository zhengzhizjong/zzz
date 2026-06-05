package com.zhongjitang.store.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.StoreRoomCreateRequest;
import com.zhongjitang.store.domain.dto.StoreRoomUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreRoomDO;

import java.util.List;

public interface IStoreRoomService {

    R<PageResult<StoreRoomDO>> page(Integer page, Integer pageSize, Long storeId, Integer roomType, Integer status);

    R<StoreRoomDO> getById(Long id);

    R<Void> create(StoreRoomCreateRequest request);

    R<Void> update(Long id, StoreRoomUpdateRequest request);

    R<Void> updateStatus(Long id, Integer status);

    R<List<StoreRoomDO>> getAvailableRooms(Long storeId);
}
