package com.zhongjitang.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.content.domain.dto.ServiceItemCreateRequest;
import com.zhongjitang.content.domain.dto.ServiceItemUpdateRequest;
import com.zhongjitang.content.domain.entity.ContentServiceItemDO;
import com.zhongjitang.content.mapper.ContentServiceItemMapper;
import com.zhongjitang.content.service.impl.ServiceItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceItemServiceImplTest {

    @Mock
    private ContentServiceItemMapper serviceItemMapper;

    @InjectMocks
    private ServiceItemServiceImpl serviceItemService;

    private ContentServiceItemDO testItem;

    @BeforeEach
    void setUp() {
        testItem = new ContentServiceItemDO();
        testItem.setId(1L);
        testItem.setItemNo("SI202606040001");
        testItem.setItemName("肩颈调理");
        testItem.setPrice(new BigDecimal("298.00"));
        testItem.setDurationMinutes(60);
        testItem.setStatus(1);
        testItem.setSortOrder(0);
    }

    @Test
    void testCreateServiceItem() {
        ServiceItemCreateRequest request = new ServiceItemCreateRequest();
        request.setItemName("肩颈调理");
        request.setPrice(new BigDecimal("298.00"));
        request.setDurationMinutes(60);
        request.setCategoryName("推拿");
        request.setCommissionType(1);
        request.setCommissionValue(BigDecimal.ZERO);

        when(serviceItemMapper.insert(any(ContentServiceItemDO.class))).thenReturn(1);

        R<Void> result = serviceItemService.create(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(serviceItemMapper, times(1)).insert(any(ContentServiceItemDO.class));
    }

    @Test
    void testPageQuery() {
        Page<ContentServiceItemDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(testItem));
        pageResult.setTotal(1);

        when(serviceItemMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        R<PageResult<ContentServiceItemDO>> result = serviceItemService.page(1, 20, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        assertEquals(1L, result.getData().getPagination().getTotal());
    }

    @Test
    void testGetById() {
        when(serviceItemMapper.selectById(1L)).thenReturn(testItem);

        R<ContentServiceItemDO> result = serviceItemService.getById(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("肩颈调理", result.getData().getItemName());
    }

    @Test
    void testGetByIdNotFound() {
        when(serviceItemMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            serviceItemService.getById(999L);
        });
        assertEquals(ErrorCode.SERVICE_ITEM_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testUpdateServiceItem() {
        when(serviceItemMapper.selectById(1L)).thenReturn(testItem);
        when(serviceItemMapper.updateById(any(ContentServiceItemDO.class))).thenReturn(1);

        ServiceItemUpdateRequest request = new ServiceItemUpdateRequest();
        request.setItemName("全身调理");
        request.setPrice(new BigDecimal("398.00"));

        R<Void> result = serviceItemService.update(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(serviceItemMapper, times(1)).updateById(any(ContentServiceItemDO.class));
    }

    @Test
    void testUpdateNotFound() {
        when(serviceItemMapper.selectById(999L)).thenReturn(null);

        ServiceItemUpdateRequest request = new ServiceItemUpdateRequest();
        request.setItemName("全身调理");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            serviceItemService.update(999L, request);
        });
        assertEquals(ErrorCode.SERVICE_ITEM_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testUpdateStatusOnline() {
        testItem.setStatus(2);
        when(serviceItemMapper.selectById(1L)).thenReturn(testItem);
        when(serviceItemMapper.updateById(any(ContentServiceItemDO.class))).thenReturn(1);

        R<Void> result = serviceItemService.updateStatus(1L, 1);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(serviceItemMapper, times(1)).updateById(any(ContentServiceItemDO.class));
    }

    @Test
    void testUpdateStatusOffline() {
        when(serviceItemMapper.selectById(1L)).thenReturn(testItem);
        when(serviceItemMapper.updateById(any(ContentServiceItemDO.class))).thenReturn(1);

        R<Void> result = serviceItemService.updateStatus(1L, 2);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(serviceItemMapper, times(1)).updateById(any(ContentServiceItemDO.class));
    }

    @Test
    void testUpdateStatusInvalidTransition() {
        testItem.setStatus(3);
        when(serviceItemMapper.selectById(1L)).thenReturn(testItem);

        R<Void> result = serviceItemService.updateStatus(1L, 1);

        assertNotNull(result);
        assertNotEquals(0, result.getCode());
    }

    @Test
    void testUpdateStatusNotFound() {
        when(serviceItemMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            serviceItemService.updateStatus(999L, 1);
        });
        assertEquals(ErrorCode.SERVICE_ITEM_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testListAll() {
        when(serviceItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testItem));

        R<List<ContentServiceItemDO>> result = serviceItemService.listAll(null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void testListAllWithStatus() {
        when(serviceItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testItem));

        R<List<ContentServiceItemDO>> result = serviceItemService.listAll(1);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
    }
}
