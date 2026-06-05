package com.zhongjitang.store.service;

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
import com.zhongjitang.store.service.impl.StoreRoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreRoomServiceImplTest {

    @Mock
    private StoreRoomMapper storeRoomMapper;

    @InjectMocks
    private StoreRoomServiceImpl storeRoomService;

    private StoreRoomDO mockRoom;

    @BeforeEach
    void setUp() {
        mockRoom = new StoreRoomDO();
        mockRoom.setId(1L);
        mockRoom.setStoreId(1L);
        mockRoom.setRoomNo("R001");
        mockRoom.setRoomName("普通1号房");
        mockRoom.setRoomType(1);
        mockRoom.setFloor(1);
        mockRoom.setCapacity(2);
        mockRoom.setStatus(1);
        mockRoom.setEquipment("空调、电视");
    }

    @Test
    void testCreateRoom() {
        StoreRoomCreateRequest request = new StoreRoomCreateRequest();
        request.setStoreId(1L);
        request.setRoomNo("R002");
        request.setRoomName("VIP1号房");
        request.setRoomType(2);
        request.setFloor(2);
        request.setCapacity(3);
        request.setEquipment("空调、电视、独立卫浴");

        when(storeRoomMapper.insert(any(StoreRoomDO.class))).thenReturn(1);

        R<Void> result = storeRoomService.create(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(storeRoomMapper, times(1)).insert(any(StoreRoomDO.class));
    }

    @Test
    void testPageQuery() {
        Page<StoreRoomDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(mockRoom));
        pageResult.setTotal(1);

        when(storeRoomMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        R<PageResult<StoreRoomDO>> result = storeRoomService.page(1, 20, 1L, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
    }

    @Test
    void testGetById() {
        when(storeRoomMapper.selectById(1L)).thenReturn(mockRoom);

        R<StoreRoomDO> result = storeRoomService.getById(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("R001", result.getData().getRoomNo());
    }

    @Test
    void testGetByIdNotFound() {
        when(storeRoomMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            storeRoomService.getById(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testUpdateRoom() {
        StoreRoomUpdateRequest request = new StoreRoomUpdateRequest();
        request.setRoomName("更新后房间名");

        when(storeRoomMapper.selectById(1L)).thenReturn(mockRoom);
        when(storeRoomMapper.updateById(any(StoreRoomDO.class))).thenReturn(1);

        R<Void> result = storeRoomService.update(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(storeRoomMapper, times(1)).updateById(any(StoreRoomDO.class));
    }

    @Test
    void testUpdateStatus() {
        when(storeRoomMapper.selectById(1L)).thenReturn(mockRoom);
        when(storeRoomMapper.updateById(any(StoreRoomDO.class))).thenReturn(1);

        R<Void> result = storeRoomService.updateStatus(1L, 2);

        assertNotNull(result);
        assertEquals(0, result.getCode());
    }

    @Test
    void testUpdateStatusInvalid() {
        when(storeRoomMapper.selectById(1L)).thenReturn(mockRoom);

        R<Void> result = storeRoomService.updateStatus(1L, 5);

        assertNotNull(result);
        assertNotEquals(0, result.getCode());
    }

    @Test
    void testGetAvailableRooms() {
        when(storeRoomMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(mockRoom));

        R<List<StoreRoomDO>> result = storeRoomService.getAvailableRooms(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
    }
}
