package com.zhongjitang.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.common.security.util.JwtUtil;
import com.zhongjitang.user.domain.dto.EmployeeCreateRequest;
import com.zhongjitang.user.domain.dto.EmployeeLoginRequest;
import com.zhongjitang.user.domain.dto.EmployeeUpdateRequest;
import com.zhongjitang.user.domain.entity.UserEmployeeDO;
import com.zhongjitang.user.domain.vo.LoginResponse;
import com.zhongjitang.user.mapper.UserEmployeeMapper;
import com.zhongjitang.user.service.impl.EmployeeServiceImpl;
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
class EmployeeServiceImplTest {

    @Mock
    private UserEmployeeMapper employeeMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private UserEmployeeDO mockEmployee;

    @BeforeEach
    void setUp() {
        mockEmployee = new UserEmployeeDO();
        mockEmployee.setId(1L);
        mockEmployee.setEmployeeNo("E202606050001");
        mockEmployee.setName("张技师");
        mockEmployee.setPhone("13900139000");
        mockEmployee.setStatus(1);
    }

    @Test
    void testCreateEmployee() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setName("新员工");
        request.setPhone("13700137000");
        request.setGender(1);
        request.setPosition("理疗师");
        request.setHireDate(LocalDate.now());

        when(employeeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(employeeMapper.insert(any(UserEmployeeDO.class))).thenReturn(1);

        R<Void> result = employeeService.create(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(employeeMapper, times(1)).insert(any(UserEmployeeDO.class));
    }

    @Test
    void testCreateEmployeeDuplicatePhone() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setName("新员工");
        request.setPhone("13900139000");

        when(employeeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            employeeService.create(request);
        });
        assertEquals(ErrorCode.DUPLICATE_PHONE.getCode(), exception.getCode());
    }

    @Test
    void testUpdateEmployee() {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        request.setName("更新名字");
        request.setPosition("高级理疗师");

        when(employeeMapper.selectById(1L)).thenReturn(mockEmployee);
        when(employeeMapper.updateById(any(UserEmployeeDO.class))).thenReturn(1);

        R<Void> result = employeeService.update(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(employeeMapper, times(1)).updateById(any(UserEmployeeDO.class));
    }

    @Test
    void testUpdateEmployeeNotFound() {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest();
        request.setName("更新名字");

        when(employeeMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            employeeService.update(999L, request);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testUpdateStatus() {
        when(employeeMapper.selectById(1L)).thenReturn(mockEmployee);
        when(employeeMapper.updateById(any(UserEmployeeDO.class))).thenReturn(1);

        R<Void> result = employeeService.updateStatus(1L, 0);

        assertNotNull(result);
        assertEquals(0, result.getCode());
    }

    @Test
    void testPage() {
        Page<UserEmployeeDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(mockEmployee));
        pageResult.setTotal(1);

        when(employeeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        R<PageResult<UserEmployeeDO>> result = employeeService.page(1, 20, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
    }

    @Test
    void testLoginEmployeeNotFound() {
        EmployeeLoginRequest request = new EmployeeLoginRequest();
        request.setPhone("13900139000");
        request.setPassword("password");

        when(employeeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            employeeService.login(request);
        });
        assertEquals(ErrorCode.LOGIN_FAILED.getCode(), exception.getCode());
    }
}
