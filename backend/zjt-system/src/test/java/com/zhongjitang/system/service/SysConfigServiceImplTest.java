package com.zhongjitang.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.entity.SysConfigDO;
import com.zhongjitang.system.mapper.SysConfigMapper;
import com.zhongjitang.system.service.impl.SysConfigServiceImpl;
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
class SysConfigServiceImplTest {

    @Mock
    private SysConfigMapper sysConfigMapper;

    @InjectMocks
    private SysConfigServiceImpl sysConfigService;

    private SysConfigDO testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new SysConfigDO();
        testConfig.setId(1L);
        testConfig.setConfigKey("system.name");
        testConfig.setConfigValue("忠济堂");
        testConfig.setConfigName("系统名称");
        testConfig.setDescription("系统名称配置");
        testConfig.setConfigType(1);
    }

    @Test
    void testList() {
        when(sysConfigMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testConfig));

        R<List<SysConfigDO>> result = sysConfigService.list();

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("system.name", result.getData().get(0).getConfigKey());
    }

    @Test
    void testGetByKey() {
        when(sysConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testConfig);

        R<SysConfigDO> result = sysConfigService.getByKey("system.name");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("忠济堂", result.getData().getConfigValue());
    }

    @Test
    void testGetByKeyNotFound() {
        when(sysConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sysConfigService.getByKey("nonexistent");
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testUpdate() {
        when(sysConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testConfig);
        when(sysConfigMapper.updateById(any(SysConfigDO.class))).thenReturn(1);

        R<Void> result = sysConfigService.update("system.name", "新名称");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(sysConfigMapper, times(1)).updateById(any(SysConfigDO.class));
    }

    @Test
    void testUpdateNotFound() {
        when(sysConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sysConfigService.update("nonexistent", "value");
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }
}
