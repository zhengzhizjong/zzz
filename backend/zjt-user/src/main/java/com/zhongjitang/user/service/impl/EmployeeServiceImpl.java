package com.zhongjitang.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.context.TenantContext;
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
import com.zhongjitang.user.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements IEmployeeService {

    private final UserEmployeeMapper employeeMapper;
    private final JwtUtil jwtUtil;
    private final javax.sql.DataSource dataSource;

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    @Override
    public R<LoginResponse> login(EmployeeLoginRequest request) {
        // 查找员工
        LambdaQueryWrapper<UserEmployeeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEmployeeDO::getPhone, request.getPhone());
        UserEmployeeDO employee = employeeMapper.selectOne(wrapper);
        if (employee == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "员工不存在");
        }

        // 验证密码（测试环境：支持固定密码123456或手机号后6位）
        String hashedPassword = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        String phoneSuffix = employee.getPhone().substring(employee.getPhone().length() - 6);
        boolean passwordMatch = "123456".equals(request.getPassword())
                || hashedPassword.equals(employee.getEmployeeNo())
                || request.getPassword().equals(phoneSuffix);
        if (!passwordMatch) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "密码错误");
        }

        if (employee.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "员工账号已被禁用");
        }

        // 更新最后登录时间
        employee.setLastLoginAt(java.time.LocalDateTime.now());
        employeeMapper.updateById(employee);

        // 生成Token
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = 1L; // 默认租户
        }
        Long storeId = employee.getStoreId();
        if (storeId == null) {
            storeId = 0L; // 默认门店
        }
        String token = jwtUtil.generateToken(employee.getId(), employee.getName(), "employee", tenantId, storeId);
        String refreshToken = jwtUtil.generateToken(employee.getId(), employee.getName(), "employee", tenantId, storeId);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setMemberId(employee.getId());
        response.setNickname(employee.getName());
        response.setAvatarUrl(employee.getAvatarUrl());
        response.setPhone(employee.getPhone());
        response.setStoreId(storeId);
        response.setTenantId(tenantId);
        response.setPosition(employee.getPosition());
        response.setEmployeeNo(employee.getEmployeeNo());
        response.setId(employee.getId());
        // 如果是理疗师，查询对应的技师ID
        if ("理疗师".equals(employee.getPosition()) || "therapist".equals(employee.getPosition())) {
            try {
                // 通过storeId和employeeId查找技师记录
                Long techId = findTechnicianIdByEmployeeId(employee.getId());
                response.setTechnicianId(techId);
            } catch (Exception e) {
                log.warn("查询技师ID失败: {}", e.getMessage());
            }
        }
        return R.ok(response);
    }

    @Override
    public R<UserEmployeeDO> getById(Long id) {
        UserEmployeeDO employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "员工不存在");
        }
        return R.ok(employee);
    }

    @Override
    public R<PageResult<UserEmployeeDO>> page(Integer page, Integer pageSize, String keyword, Integer status, Long departmentId) {
        Page<UserEmployeeDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserEmployeeDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(UserEmployeeDO::getName, keyword)
                    .or().like(UserEmployeeDO::getPhone, keyword)
                    .or().like(UserEmployeeDO::getEmployeeNo, keyword));
        }
        if (status != null) {
            wrapper.eq(UserEmployeeDO::getStatus, status);
        }
        if (departmentId != null) {
            wrapper.eq(UserEmployeeDO::getDepartmentId, departmentId);
        }
        wrapper.orderByDesc(UserEmployeeDO::getCreatedAt);
        Page<UserEmployeeDO> result = employeeMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<Void> create(EmployeeCreateRequest request) {
        // 检查手机号是否已存在
        LambdaQueryWrapper<UserEmployeeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEmployeeDO::getPhone, request.getPhone());
        Long count = employeeMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE, "员工手机号已存在");
        }

        UserEmployeeDO employee = new UserEmployeeDO();
        employee.setEmployeeNo(generateEmployeeNo());
        employee.setName(request.getName());
        employee.setPhone(request.getPhone());
        employee.setGender(request.getGender());
        employee.setAvatarUrl(request.getAvatarUrl());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setPosition(request.getPosition());
        employee.setHireDate(request.getHireDate() != null ? request.getHireDate() : LocalDate.now());
        employee.setStatus(1);
        employeeMapper.insert(employee);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, EmployeeUpdateRequest request) {
        UserEmployeeDO employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "员工不存在");
        }

        if (request.getName() != null) {
            employee.setName(request.getName());
        }
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            employee.setGender(request.getGender());
        }
        if (request.getAvatarUrl() != null) {
            employee.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getDepartmentId() != null) {
            employee.setDepartmentId(request.getDepartmentId());
        }
        if (request.getPosition() != null) {
            employee.setPosition(request.getPosition());
        }
        if (request.getHireDate() != null) {
            employee.setHireDate(request.getHireDate());
        }

        employeeMapper.updateById(employee);
        return R.ok();
    }

    @Override
    public R<Void> updateStatus(Long id, Integer status) {
        UserEmployeeDO employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "员工不存在");
        }
        employee.setStatus(status);
        employeeMapper.updateById(employee);
        return R.ok();
    }

    private String generateEmployeeNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = SEQUENCE.getAndIncrement();
        if (seq > 9999) {
            SEQUENCE.set(1);
            seq = 1;
        }
        return "E" + datePart + String.format("%04d", seq);
    }

    /**
     * 通过员工ID查找对应的技师ID
     * 在store_technician表中，employee_id字段关联员工ID
     */
    private Long findTechnicianIdByEmployeeId(Long employeeId) {
        try {
            java.sql.Connection conn = dataSource.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM store_technician WHERE employee_id = ? AND deleted = 0 LIMIT 1");
            ps.setLong(1, employeeId);
            java.sql.ResultSet rs = ps.executeQuery();
            Long techId = null;
            if (rs.next()) {
                techId = rs.getLong("id");
            }
            rs.close();
            ps.close();
            conn.close();
            return techId;
        } catch (Exception e) {
            log.warn("查询技师ID失败: {}", e.getMessage());
        }
        return null;
    }
}
