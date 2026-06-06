package com.zhongjitang.store.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.store.domain.dto.TechnicianCreateRequest;
import com.zhongjitang.store.domain.dto.TechnicianUpdateRequest;
import com.zhongjitang.store.domain.entity.StoreTechnicianDO;
import com.zhongjitang.store.domain.vo.TechnicianWorkspaceVO;
import com.zhongjitang.store.mapper.StoreTechnicianMapper;
import com.zhongjitang.store.service.impl.StoreTechnicianServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("门店技师服务测试")
class StoreTechnicianServiceTest {

    @Mock
    private StoreTechnicianMapper storeTechnicianMapper;

    @InjectMocks
    private StoreTechnicianServiceImpl storeTechnicianService;

    private StoreTechnicianDO mockTechnician;

    @BeforeEach
    void setUp() {
        mockTechnician = new StoreTechnicianDO();
        mockTechnician.setId(1L);
        mockTechnician.setStoreId(1L);
        mockTechnician.setEmployeeId(1L);
        mockTechnician.setTechnicianNo("T202606040001");
        mockTechnician.setSkillLevel(1);
        mockTechnician.setStatus(1);
        mockTechnician.setIsOnline(0);
        mockTechnician.setMonthServiceCount(10);
        mockTechnician.setMonthRevenue(new BigDecimal("15000"));
        mockTechnician.setMonthRating(new BigDecimal("4.8"));
    }

    @Test
    @DisplayName("创建技师-正常流程")
    void 创建技师成功() {
        TechnicianCreateRequest request = new TechnicianCreateRequest();
        request.setStoreId(1L);
        request.setEmployeeId(1L);
        request.setSkillLevel(1);
        request.setSkilledItems("[\"推拿\",\"艾灸\"]");
        request.setDefaultSchedule("早班");

        when(storeTechnicianMapper.insert(any(StoreTechnicianDO.class))).thenReturn(1);

        R<Void> result = storeTechnicianService.create(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("success", result.getMessage());
        verify(storeTechnicianMapper, times(1)).insert(any(StoreTechnicianDO.class));
    }

    @Test
    @DisplayName("分页查询技师-正常流程")
    void 分页查询技师() {
        Page<StoreTechnicianDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(mockTechnician));
        pageResult.setTotal(1);

        when(storeTechnicianMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        R<PageResult<StoreTechnicianDO>> result = storeTechnicianService.page(1, 20, 1L, null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        assertEquals(1L, result.getData().getList().get(0).getStoreId());
    }

    @Test
    @DisplayName("根据ID查询技师-正常流程")
    void 根据ID查询技师成功() {
        when(storeTechnicianMapper.selectById(1L)).thenReturn(mockTechnician);

        R<StoreTechnicianDO> result = storeTechnicianService.getById(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("T202606040001", result.getData().getTechnicianNo());
        assertEquals(1, result.getData().getSkillLevel());
    }

    @Test
    @DisplayName("根据ID查询技师-不存在抛异常")
    void 根据ID查询技师不存在() {
        when(storeTechnicianMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            storeTechnicianService.getById(999L);
        });

        assertEquals(ErrorCode.TECHNICIAN_NOT_FOUND.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("技师不存在"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    @DisplayName("更新技师信息-正常流程")
    void 更新技师信息成功() {
        TechnicianUpdateRequest request = new TechnicianUpdateRequest();
        request.setSkillLevel(2);
        request.setSkilledItems("[\"推拿\",\"艾灸\",\"拔罐\"]");

        when(storeTechnicianMapper.selectById(1L)).thenReturn(mockTechnician);
        when(storeTechnicianMapper.updateById(any(StoreTechnicianDO.class))).thenReturn(1);

        R<Void> result = storeTechnicianService.update(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(2, mockTechnician.getSkillLevel());
        assertEquals("[\"推拿\",\"艾灸\",\"拔罐\"]", mockTechnician.getSkilledItems());
        verify(storeTechnicianMapper, times(1)).updateById(any(StoreTechnicianDO.class));
    }

    @Test
    @DisplayName("技师签到-正常流程")
    void 技师签到成功() {
        when(storeTechnicianMapper.selectById(1L)).thenReturn(mockTechnician);
        when(storeTechnicianMapper.updateById(any(StoreTechnicianDO.class))).thenReturn(1);

        R<Void> result = storeTechnicianService.checkIn(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, mockTechnician.getIsOnline());
        verify(storeTechnicianMapper, times(1)).updateById(any(StoreTechnicianDO.class));
    }

    @Test
    @DisplayName("技师签退-正常流程")
    void 技师签退成功() {
        mockTechnician.setIsOnline(1);
        when(storeTechnicianMapper.selectById(1L)).thenReturn(mockTechnician);
        when(storeTechnicianMapper.updateById(any(StoreTechnicianDO.class))).thenReturn(1);

        R<Void> result = storeTechnicianService.checkOut(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(0, mockTechnician.getIsOnline());
        verify(storeTechnicianMapper, times(1)).updateById(any(StoreTechnicianDO.class));
    }

    @Test
    @DisplayName("获取技师工作台-正常流程")
    void 获取技师工作台() {
        when(storeTechnicianMapper.selectById(1L)).thenReturn(mockTechnician);

        R<TechnicianWorkspaceVO> result = storeTechnicianService.getWorkspace(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getTechnicianInfo());
        assertEquals(1L, result.getData().getTechnicianInfo().getId());
        assertEquals(1, result.getData().getTechnicianInfo().getSkillLevel());
    }

    @Test
    @DisplayName("获取技师工作台-不存在抛异常")
    void 获取技师工作台不存在() {
        when(storeTechnicianMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            storeTechnicianService.getWorkspace(999L);
        });

        assertEquals(ErrorCode.TECHNICIAN_NOT_FOUND.getCode(), exception.getCode());
    }
}
