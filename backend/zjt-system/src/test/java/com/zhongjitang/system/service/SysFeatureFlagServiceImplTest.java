package com.zhongjitang.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.FeatureFlagCreateRequest;
import com.zhongjitang.system.domain.dto.FeatureFlagUpdateRequest;
import com.zhongjitang.system.domain.entity.SysFeatureFlagDO;
import com.zhongjitang.system.mapper.SysFeatureFlagMapper;
import com.zhongjitang.system.service.impl.SysFeatureFlagServiceImpl;
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
class SysFeatureFlagServiceImplTest {

    @Mock
    private SysFeatureFlagMapper sysFeatureFlagMapper;

    @InjectMocks
    private SysFeatureFlagServiceImpl sysFeatureFlagService;

    private SysFeatureFlagDO testFlag;

    @BeforeEach
    void setUp() {
        testFlag = new SysFeatureFlagDO();
        testFlag.setId(1L);
        testFlag.setFlagKey("ai_recommendation");
        testFlag.setFlagName("AI推荐功能");
        testFlag.setDescription("是否启用AI推荐功能");
        testFlag.setDefaultValue(1);
        testFlag.setType(1);
        testFlag.setPercentage(null);
        testFlag.setRulesJson(null);
    }

    @Test
    void testList() {
        when(sysFeatureFlagMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testFlag));

        R<List<SysFeatureFlagDO>> result = sysFeatureFlagService.list();

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("ai_recommendation", result.getData().get(0).getFlagKey());
    }

    @Test
    void testGetByKey() {
        when(sysFeatureFlagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testFlag);

        R<SysFeatureFlagDO> result = sysFeatureFlagService.getByKey("ai_recommendation");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("AI推荐功能", result.getData().getFlagName());
    }

    @Test
    void testGetByKeyNotFound() {
        when(sysFeatureFlagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sysFeatureFlagService.getByKey("nonexistent");
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testCreate() {
        FeatureFlagCreateRequest request = new FeatureFlagCreateRequest();
        request.setFlagKey("new_feature");
        request.setFlagName("新功能");
        request.setType(1);
        request.setDefaultValue(0);

        when(sysFeatureFlagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(sysFeatureFlagMapper.insert(any(SysFeatureFlagDO.class))).thenReturn(1);

        R<Void> result = sysFeatureFlagService.create(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(sysFeatureFlagMapper, times(1)).insert(any(SysFeatureFlagDO.class));
    }

    @Test
    void testCreateDuplicateKey() {
        FeatureFlagCreateRequest request = new FeatureFlagCreateRequest();
        request.setFlagKey("ai_recommendation");
        request.setFlagName("AI推荐功能");

        when(sysFeatureFlagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        R<Void> result = sysFeatureFlagService.create(request);

        assertNotNull(result);
        assertNotEquals(0, result.getCode());
        verify(sysFeatureFlagMapper, never()).insert(any(SysFeatureFlagDO.class));
    }

    @Test
    void testUpdate() {
        when(sysFeatureFlagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testFlag);
        when(sysFeatureFlagMapper.updateById(any(SysFeatureFlagDO.class))).thenReturn(1);

        FeatureFlagUpdateRequest request = new FeatureFlagUpdateRequest();
        request.setFlagName("AI推荐功能V2");
        request.setDefaultValue(0);

        R<Void> result = sysFeatureFlagService.update("ai_recommendation", request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(sysFeatureFlagMapper, times(1)).updateById(any(SysFeatureFlagDO.class));
    }

    @Test
    void testUpdateNotFound() {
        when(sysFeatureFlagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        FeatureFlagUpdateRequest request = new FeatureFlagUpdateRequest();
        request.setFlagName("新名称");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sysFeatureFlagService.update("nonexistent", request);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testIsEnabledGlobalOn() {
        testFlag.setType(1);
        testFlag.setDefaultValue(1);

        when(sysFeatureFlagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testFlag);

        R<Boolean> result = sysFeatureFlagService.isEnabled("ai_recommendation", null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertTrue(result.getData());
    }

    @Test
    void testIsEnabledGlobalOff() {
        testFlag.setType(1);
        testFlag.setDefaultValue(0);

        when(sysFeatureFlagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testFlag);

        R<Boolean> result = sysFeatureFlagService.isEnabled("ai_recommendation", null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertFalse(result.getData());
    }

    @Test
    void testIsEnabledNotFound() {
        when(sysFeatureFlagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        R<Boolean> result = sysFeatureFlagService.isEnabled("nonexistent", null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertFalse(result.getData());
    }

    @Test
    void testIsEnabledTenantWithRule() {
        testFlag.setType(2);
        testFlag.setDefaultValue(0);
        testFlag.setRulesJson("[1001, 1002, 1003]");

        when(sysFeatureFlagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testFlag);

        R<Boolean> result = sysFeatureFlagService.isEnabled("ai_recommendation", 1002L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertTrue(result.getData());
    }

    @Test
    void testIsEnabledTenantWithoutRule() {
        testFlag.setType(2);
        testFlag.setDefaultValue(0);
        testFlag.setRulesJson("[1001, 1002, 1003]");

        when(sysFeatureFlagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testFlag);

        R<Boolean> result = sysFeatureFlagService.isEnabled("ai_recommendation", 9999L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertFalse(result.getData());
    }

    @Test
    void testIsEnabledPercentage() {
        testFlag.setType(5);
        testFlag.setDefaultValue(0);
        testFlag.setPercentage(100);

        when(sysFeatureFlagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testFlag);

        R<Boolean> result = sysFeatureFlagService.isEnabled("ai_recommendation", 1001L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertTrue(result.getData());
    }

    @Test
    void testIsEnabledPercentageZero() {
        testFlag.setType(5);
        testFlag.setDefaultValue(0);
        testFlag.setPercentage(0);

        when(sysFeatureFlagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testFlag);

        R<Boolean> result = sysFeatureFlagService.isEnabled("ai_recommendation", 1001L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertFalse(result.getData());
    }
}
