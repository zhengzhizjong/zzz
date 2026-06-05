package com.zhongjitang.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.AssignPermissionRequest;
import com.zhongjitang.user.domain.dto.RoleCreateRequest;
import com.zhongjitang.user.domain.dto.RoleUpdateRequest;
import com.zhongjitang.user.domain.entity.UserRoleDO;
import com.zhongjitang.user.domain.entity.UserRolePermissionDO;
import com.zhongjitang.user.mapper.UserRoleMapper;
import com.zhongjitang.user.mapper.UserRolePermissionMapper;
import com.zhongjitang.user.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final UserRoleMapper roleMapper;
    private final UserRolePermissionMapper rolePermissionMapper;

    @Override
    public R<List<UserRoleDO>> list() {
        LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(UserRoleDO::getId);
        List<UserRoleDO> list = roleMapper.selectList(wrapper);
        return R.ok(list);
    }

    @Override
    public R<Void> create(RoleCreateRequest request) {
        // 检查角色编码是否重复
        LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRoleDO::getRoleCode, request.getRoleCode());
        Long count = roleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }

        UserRoleDO role = new UserRoleDO();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setStatus(1);
        roleMapper.insert(role);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, RoleUpdateRequest request) {
        UserRoleDO role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }

        if (request.getRoleName() != null) {
            role.setRoleName(request.getRoleName());
        }
        if (request.getRoleCode() != null) {
            role.setRoleCode(request.getRoleCode());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            role.setStatus(request.getStatus());
        }

        roleMapper.updateById(role);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> assignPermissions(Long id, AssignPermissionRequest request) {
        UserRoleDO role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }

        // 删除原有权限
        LambdaQueryWrapper<UserRolePermissionDO> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserRolePermissionDO::getRoleId, id);
        rolePermissionMapper.delete(deleteWrapper);

        // 新增权限
        if (request.getPermissionIds() != null) {
            for (Long permissionId : request.getPermissionIds()) {
                UserRolePermissionDO rp = new UserRolePermissionDO();
                rp.setRoleId(id);
                rp.setPermissionId(permissionId);
                rolePermissionMapper.insert(rp);
            }
        }

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> delete(Long id) {
        UserRoleDO role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }

        // 删除角色权限关联
        LambdaQueryWrapper<UserRolePermissionDO> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserRolePermissionDO::getRoleId, id);
        rolePermissionMapper.delete(deleteWrapper);

        // 删除角色
        roleMapper.deleteById(id);
        return R.ok();
    }
}
