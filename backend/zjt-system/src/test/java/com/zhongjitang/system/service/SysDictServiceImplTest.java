package com.zhongjitang.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.SysDictCreateRequest;
import com.zhongjitang.system.domain.dto.SysDictItemCreateRequest;
import com.zhongjitang.system.domain.dto.SysDictItemUpdateRequest;
import com.zhongjitang.system.domain.dto.SysDictUpdateRequest;
import com.zhongjitang.system.domain.entity.SysDictDO;
import com.zhongjitang.system.domain.entity.SysDictItemDO;
import com.zhongjitang.system.mapper.SysDictItemMapper;
import com.zhongjitang.system.mapper.SysDictMapper;
import com.zhongjitang.system.service.impl.SysDictServiceImpl;
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
class SysDictServiceImplTest {

    @Mock
    private SysDictMapper sysDictMapper;

    @Mock
    private SysDictItemMapper sysDictItemMapper;

    @InjectMocks
    private SysDictServiceImpl sysDictService;

    private SysDictDO testDict;
    private SysDictItemDO testDictItem;

    @BeforeEach
    void setUp() {
        testDict = new SysDictDO();
        testDict.setId(1L);
        testDict.setDictCode("gender");
        testDict.setDictName("性别");
        testDict.setStatus(1);

        testDictItem = new SysDictItemDO();
        testDictItem.setId(1L);
        testDictItem.setDictId(1L);
        testDictItem.setItemCode("male");
        testDictItem.setItemName("男");
        testDictItem.setItemValue("1");
        testDictItem.setSortOrder(1);
        testDictItem.setStatus(1);
    }

    @Test
    void testPageQuery() {
        Page<SysDictDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(testDict));
        pageResult.setTotal(1);

        when(sysDictMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        R<PageResult<SysDictDO>> result = sysDictService.page(1, 20, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        assertEquals(1L, result.getData().getPagination().getTotal());
    }

    @Test
    void testPageQueryWithKeyword() {
        Page<SysDictDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(testDict));
        pageResult.setTotal(1);

        when(sysDictMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        R<PageResult<SysDictDO>> result = sysDictService.page(1, 20, "性别");

        assertNotNull(result);
        assertEquals(0, result.getCode());
    }

    @Test
    void testGetById() {
        when(sysDictMapper.selectById(1L)).thenReturn(testDict);

        R<SysDictDO> result = sysDictService.getById(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("gender", result.getData().getDictCode());
        assertEquals("性别", result.getData().getDictName());
    }

    @Test
    void testGetByIdNotFound() {
        when(sysDictMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sysDictService.getById(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testCreateDict() {
        SysDictCreateRequest request = new SysDictCreateRequest();
        request.setDictCode("gender");
        request.setDictName("性别");

        when(sysDictMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(sysDictMapper.insert(any(SysDictDO.class))).thenReturn(1);

        R<Void> result = sysDictService.create(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(sysDictMapper, times(1)).insert(any(SysDictDO.class));
    }

    @Test
    void testCreateDictDuplicateCode() {
        SysDictCreateRequest request = new SysDictCreateRequest();
        request.setDictCode("gender");
        request.setDictName("性别");

        when(sysDictMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        R<Void> result = sysDictService.create(request);

        assertNotNull(result);
        assertNotEquals(0, result.getCode());
        verify(sysDictMapper, never()).insert(any(SysDictDO.class));
    }

    @Test
    void testUpdateDict() {
        when(sysDictMapper.selectById(1L)).thenReturn(testDict);
        when(sysDictMapper.updateById(any(SysDictDO.class))).thenReturn(1);

        SysDictUpdateRequest request = new SysDictUpdateRequest();
        request.setDictName("性别类型");

        R<Void> result = sysDictService.update(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(sysDictMapper, times(1)).updateById(any(SysDictDO.class));
    }

    @Test
    void testUpdateDictNotFound() {
        when(sysDictMapper.selectById(999L)).thenReturn(null);

        SysDictUpdateRequest request = new SysDictUpdateRequest();
        request.setDictName("性别类型");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sysDictService.update(999L, request);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testGetItems() {
        when(sysDictMapper.selectById(1L)).thenReturn(testDict);
        when(sysDictItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testDictItem));

        R<List<SysDictItemDO>> result = sysDictService.getItems(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("male", result.getData().get(0).getItemCode());
    }

    @Test
    void testAddItem() {
        when(sysDictMapper.selectById(1L)).thenReturn(testDict);
        when(sysDictItemMapper.insert(any(SysDictItemDO.class))).thenReturn(1);

        SysDictItemCreateRequest request = new SysDictItemCreateRequest();
        request.setDictId(1L);
        request.setItemCode("female");
        request.setItemName("女");
        request.setItemValue("2");

        R<Void> result = sysDictService.addItem(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(sysDictItemMapper, times(1)).insert(any(SysDictItemDO.class));
    }

    @Test
    void testUpdateItem() {
        when(sysDictItemMapper.selectById(1L)).thenReturn(testDictItem);
        when(sysDictItemMapper.updateById(any(SysDictItemDO.class))).thenReturn(1);

        SysDictItemUpdateRequest request = new SysDictItemUpdateRequest();
        request.setItemName("男性");

        R<Void> result = sysDictService.updateItem(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(sysDictItemMapper, times(1)).updateById(any(SysDictItemDO.class));
    }

    @Test
    void testUpdateItemNotFound() {
        when(sysDictItemMapper.selectById(999L)).thenReturn(null);

        SysDictItemUpdateRequest request = new SysDictItemUpdateRequest();
        request.setItemName("男性");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sysDictService.updateItem(999L, request);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testDeleteItem() {
        when(sysDictItemMapper.selectById(1L)).thenReturn(testDictItem);
        when(sysDictItemMapper.deleteById(1L)).thenReturn(1);

        R<Void> result = sysDictService.deleteItem(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(sysDictItemMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteItemNotFound() {
        when(sysDictItemMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sysDictService.deleteItem(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testGetItemsByCode() {
        when(sysDictMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testDict);
        when(sysDictItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testDictItem));

        R<List<SysDictItemDO>> result = sysDictService.getItemsByCode("gender");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void testGetItemsByCodeNotFound() {
        when(sysDictMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sysDictService.getItemsByCode("nonexistent");
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }
}
