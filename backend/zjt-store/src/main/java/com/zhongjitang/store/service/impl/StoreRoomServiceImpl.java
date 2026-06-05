package com.zhongjitang.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.StoreRoomCreateRequest;
import com.zhongjitang.store.domain.dto.StoreRoomUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreRoomDO;
import com.zhongjitang.store.mapper.StoreRoomMapper;
import com.zhongjitang.store.service.IStoreRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreRoomServiceImpl implements IStoreRoomService {

    private final StoreRoomMapper storeRoomMapper;

    @Override
    public R<PageResult<StoreRoomDO>> page(Integer page, Integer pageSize, Long storeId, Integer roomType, Integer status) {
        Page<StoreRoomDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<StoreRoomDO> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) {
            wrapper.eq(StoreRoomDO::getStoreId, storeId);
        }
        if (roomType != null) {
            wrapper.eq(StoreRoomDO::getRoomType, roomType);
        }
        if (status != null) {
            wrapper.eq(StoreRoomDO::getStatus, status);
        }
        wrapper.orderByDesc(StoreRoomDO::getCreatedAt);
        Page<StoreRoomDO> result = storeRoomMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<StoreRoomDO> getById(Long id) {
        StoreRoomDO room = storeRoomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "房间不存在，ID: " + id);
        }
        return R.ok(room);
    }

    @Override
    public R<Void> create(StoreRoomCreateRequest request) {
        StoreRoomDO room = new StoreRoomDO();
        room.setStoreId(request.getStoreId());
        room.setRoomNo(request.getRoomNo());
        room.setRoomName(request.getRoomName());
        room.setRoomType(request.getRoomType());
        room.setFloor(request.getFloor());
        room.setCapacity(request.getCapacity());
        room.setStatus(1);
        room.setEquipment(request.getEquipment());
        storeRoomMapper.insert(room);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, StoreRoomUpdateRequest request) {
        StoreRoomDO room = storeRoomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "房间不存在，ID: " + id);
        }
        if (request.getRoomNo() != null) {
            room.setRoomNo(request.getRoomNo());
        }
        if (request.getRoomName() != null) {
            room.setRoomName(request.getRoomName());
        }
        if (request.getRoomType() != null) {
            room.setRoomType(request.getRoomType());
        }
        if (request.getFloor() != null) {
            room.setFloor(request.getFloor());
        }
        if (request.getCapacity() != null) {
            room.setCapacity(request.getCapacity());
        }
        if (request.getEquipment() != null) {
            room.setEquipment(request.getEquipment());
        }
        storeRoomMapper.updateById(room);
        return R.ok();
    }

    @Override
    public R<Void> updateStatus(Long id, Integer status) {
        StoreRoomDO room = storeRoomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "房间不存在，ID: " + id);
        }
        if (status < 1 || status > 3) {
            return R.fail("房间状态无效，有效值: 1空闲 2使用中 3维护中");
        }
        room.setStatus(status);
        storeRoomMapper.updateById(room);
        return R.ok();
    }

    @Override
    public R<List<StoreRoomDO>> getAvailableRooms(Long storeId) {
        LambdaQueryWrapper<StoreRoomDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreRoomDO::getStoreId, storeId);
        wrapper.eq(StoreRoomDO::getStatus, 1);
        wrapper.orderByAsc(StoreRoomDO::getRoomNo);
        List<StoreRoomDO> rooms = storeRoomMapper.selectList(wrapper);
        return R.ok(rooms);
    }
}
