package com.zhongjitang.user.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.EmployeeCreateRequest;
import com.zhongjitang.user.domain.dto.EmployeeLoginRequest;
import com.zhongjitang.user.domain.dto.EmployeeUpdateRequest;
import com.zhongjitang.user.domain.entity.UserEmployeeDO;
import com.zhongjitang.user.domain.vo.LoginResponse;

public interface IEmployeeService {

    R<LoginResponse> login(EmployeeLoginRequest request);

    R<PageResult<UserEmployeeDO>> page(Integer page, Integer pageSize, String keyword, Integer status, Long departmentId);

    R<Void> create(EmployeeCreateRequest request);

    R<Void> update(Long id, EmployeeUpdateRequest request);

    R<Void> updateStatus(Long id, Integer status);
}
