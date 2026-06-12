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
import com.zhongjitang.store.mapper.StoreTechnicianMapper;
import com.zhongjitang.store.service.impl.StoreTechnicianServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreTechnicianServiceImplTest {

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
    }

    @Test
    void testCreateTechnician() {
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
        verify(storeTechnicianMapper, times(1)).insert(any(StoreTechnicianDO.class));
    }

    @Test
    void testPageQuery() {
        Page<StoreTechnicianDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(mockTechnician));
        pageResult.setTotal(1);

        when(storeTechnicianMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        R<PageResult<StoreTechnicianDO>> result = storeTechnicianService.page(1, 20, 1L, null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
    }

    @Test
    void testCheckIn() {
        when(storeTechnicianMapper.selectById(1L)).thenReturn(mockTechnician);
        when(storeTechnicianMapper.updateById(any(StoreTechnicianDO.class))).thenReturn(1);

        R<Void> result = storeTechnicianService.checkIn(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, mockTechnician.getIsOnline());
    }

    @Test
    void testCheckOut() {
        mockTechnician.setIsOnline(1);
        when(storeTechnicianMapper.selectById(1L)).thenReturn(mockTechnician);
        when(storeTechnicianMapper.updateById(any(StoreTechnicianDO.class))).thenReturn(1);

        R<Void> result = storeTechnicianService.checkOut(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(0, mockTechnician.getIsOnline());
    }

    @Test
    void testUpdateStatus() {
        when(storeTechnicianMapper.selectById(1L)).thenReturn(mockTechnician);
        when(storeTechnicianMapper.updateById(any(StoreTechnicianDO.class))).thenReturn(1);

        R<Void> result = storeTechnicianService.updateStatus(1L, 2);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(2, mockTechnician.getStatus());
    }

    @Test
    void testGetByIdNotFound() {
        when(storeTechnicianMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            storeTechnicianService.getById(999L);
        });
        assertEquals(ErrorCode.TECHNICIAN_NOT_FOUND.getCode(), exception.getCode());
    }
}
