package com.zhongjitang.store.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.StoreCreateRequest;
import com.zhongjitang.store.domain.dto.StoreUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreInfoDO;
import com.zhongjitang.store.mapper.StoreInfoMapper;
import com.zhongjitang.store.service.impl.StoreInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("门店信息服务测试")
class StoreInfoServiceTest {

    @Mock
    private StoreInfoMapper storeInfoMapper;

    @InjectMocks
    private StoreInfoServiceImpl storeInfoService;

    private StoreInfoDO mockStore;

    @BeforeEach
    void setUp() {
        mockStore = new StoreInfoDO();
        mockStore.setId(1L);
        mockStore.setStoreNo("S202606040001");
        mockStore.setStoreName("忠济堂国医馆(朝阳店)");
        mockStore.setStoreType(1);
        mockStore.setStatus(1);
        mockStore.setContactPhone("13800138000");
    }

    @Test
    @DisplayName("创建门店-正常流程")
    void 创建门店成功() {
        StoreCreateRequest request = new StoreCreateRequest();
        request.setStoreName("测试门店");
        request.setStoreType(1);
        request.setProvinceCode("110000");
        request.setCityCode("110105");
        request.setDistrictCode("110105");
        request.setAddress("测试地址");
        request.setLatitude(new BigDecimal("39.908823"));
        request.setLongitude(new BigDecimal("116.397470"));
        request.setContactName("张三");
        request.setContactPhone("13800138000");
        request.setBusinessStartTime(LocalTime.of(9, 0));
        request.setBusinessEndTime(LocalTime.of(21, 0));

        when(storeInfoMapper.insert(any(StoreInfoDO.class))).thenReturn(1);

        R<Void> result = storeInfoService.create(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("success", result.getMessage());
        verify(storeInfoMapper, times(1)).insert(any(StoreInfoDO.class));
    }

    @Test
    @DisplayName("分页查询门店-正常流程")
    void 分页查询门店() {
        Page<StoreInfoDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(mockStore));
        pageResult.setTotal(1);

        when(storeInfoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        R<PageResult<StoreInfoDO>> result = storeInfoService.page(1, 20, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        assertEquals(1, result.getData().getPagination().getTotal());
    }

    @Test
    @DisplayName("根据ID查询门店-正常流程")
    void 根据ID查询门店成功() {
        when(storeInfoMapper.selectById(1L)).thenReturn(mockStore);

        R<StoreInfoDO> result = storeInfoService.getById(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("忠济堂国医馆(朝阳店)", result.getData().getStoreName());
        assertEquals(1, result.getData().getStatus());
    }

    @Test
    @DisplayName("根据ID查询门店-不存在抛异常")
    void 根据ID查询门店不存在() {
        when(storeInfoMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            storeInfoService.getById(999L);
        });

        assertEquals(ErrorCode.STORE_NOT_FOUND.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("门店不存在"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    @DisplayName("更新门店-正常流程")
    void 更新门店成功() {
        StoreUpdateRequest request = new StoreUpdateRequest();
        request.setStoreName("更新后门店名");
        request.setContactPhone("13900139000");

        when(storeInfoMapper.selectById(1L)).thenReturn(mockStore);
        when(storeInfoMapper.updateById(any(StoreInfoDO.class))).thenReturn(1);

        R<Void> result = storeInfoService.update(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("更新后门店名", mockStore.getStoreName());
        assertEquals("13900139000", mockStore.getContactPhone());
        verify(storeInfoMapper, times(1)).updateById(any(StoreInfoDO.class));
    }

    @Test
    @DisplayName("更新门店-不存在抛异常")
    void 更新门店不存在() {
        StoreUpdateRequest request = new StoreUpdateRequest();
        request.setStoreName("更新名");

        when(storeInfoMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            storeInfoService.update(999L, request);
        });

        assertEquals(ErrorCode.STORE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("更新门店状态-营业中转休息中")
    void 更新门店状态营业转休息() {
        mockStore.setStatus(1);
        when(storeInfoMapper.selectById(1L)).thenReturn(mockStore);
        when(storeInfoMapper.updateById(any(StoreInfoDO.class))).thenReturn(1);

        R<Void> result = storeInfoService.updateStatus(1L, 2);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(2, mockStore.getStatus());
    }

    @Test
    @DisplayName("更新门店状态-无效状态转换返回失败")
    void 更新门店状态无效转换() {
        mockStore.setStatus(3);
        when(storeInfoMapper.selectById(1L)).thenReturn(mockStore);

        R<Void> result = storeInfoService.updateStatus(1L, 1);

        assertNotNull(result);
        assertNotEquals(0, result.getCode());
        verify(storeInfoMapper, never()).updateById(any(StoreInfoDO.class));
    }
}
