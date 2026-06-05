package com.zhongjitang.user.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.EmployeeCreateRequest;
import com.zhongjitang.user.domain.dto.EmployeeLoginRequest;
import com.zhongjitang.user.domain.dto.EmployeeUpdateRequest;
import com.zhongjitang.user.domain.entity.UserEmployeeDO;
import com.zhongjitang.user.domain.vo.LoginResponse;
import com.zhongjitang.user.service.IEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user/employees")
@Tag(name = "员工管理")
@RequiredArgsConstructor
public class EmployeeController {

    private final IEmployeeService employeeService;

    @PostMapping("/login")
    @Operation(summary = "员工登录")
    public R<LoginResponse> login(@Valid @RequestBody EmployeeLoginRequest request) {
        return employeeService.login(request);
    }

    @GetMapping
    @Operation(summary = "员工列表")
    public R<PageResult<UserEmployeeDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long departmentId) {
        return employeeService.page(page, pageSize, keyword, status, departmentId);
    }

    @PostMapping
    @Operation(summary = "创建员工")
    public R<Void> create(@Valid @RequestBody EmployeeCreateRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新员工")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest request) {
        return employeeService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "状态变更")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return employeeService.updateStatus(id, status);
    }
}
