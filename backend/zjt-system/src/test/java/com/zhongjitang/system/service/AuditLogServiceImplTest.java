package com.zhongjitang.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.AuditLogCreateRequest;
import com.zhongjitang.system.domain.entity.SysAuditLogDO;
import com.zhongjitang.system.mapper.SysAuditLogMapper;
import com.zhongjitang.system.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private SysAuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private SysAuditLogDO mockAuditLog;

    @BeforeEach
    void setUp() {
        mockAuditLog = new SysAuditLogDO();
        mockAuditLog.setId(1L);
        mockAuditLog.setUserId(1L);
        mockAuditLog.setUserName("admin");
        mockAuditLog.setOperation("创建门店");
        mockAuditLog.setMethod("POST");
        mockAuditLog.setModule("门店管理");
        mockAuditLog.setStatus(0);
    }

    @Test
    void testPageQuery() {
        Page<SysAuditLogDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(mockAuditLog));
        pageResult.setTotal(1);

        when(auditLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        R<PageResult<SysAuditLogDO>> result = auditLogService.page(1, 20, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
    }

    @Test
    void testPageQueryWithFilters() {
        Page<SysAuditLogDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(mockAuditLog));
        pageResult.setTotal(1);

        when(auditLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        R<PageResult<SysAuditLogDO>> result = auditLogService.page(
                1, 20, "admin", "门店管理", "创建门店",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertNotNull(result);
        assertEquals(0, result.getCode());
    }

    @Test
    void testLog() {
        AuditLogCreateRequest request = new AuditLogCreateRequest();
        request.setUserId(1L);
        request.setUserName("admin");
        request.setOperation("创建门店");
        request.setMethod("POST");
        request.setModule("门店管理");
        request.setIp("127.0.0.1");
        request.setDuration(100L);
        request.setStatus(0);

        when(auditLogMapper.insert(any(SysAuditLogDO.class))).thenReturn(1);

        R<Void> result = auditLogService.log(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(auditLogMapper, times(1)).insert(any(SysAuditLogDO.class));
    }

    @Test
    void testLogWithDefaultStatus() {
        AuditLogCreateRequest request = new AuditLogCreateRequest();
        request.setOperation("删除门店");
        request.setModule("门店管理");

        when(auditLogMapper.insert(any(SysAuditLogDO.class))).thenReturn(1);

        R<Void> result = auditLogService.log(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(auditLogMapper, times(1)).insert(any(SysAuditLogDO.class));
    }
}
