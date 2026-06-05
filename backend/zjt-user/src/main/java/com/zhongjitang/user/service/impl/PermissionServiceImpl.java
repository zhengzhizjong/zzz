package com.zhongjitang.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.PermissionCreateRequest;
import com.zhongjitang.user.domain.dto.PermissionUpdateRequest;
import com.zhongjitang.user.domain.entity.UserPermissionDO;
import com.zhongjitang.user.domain.vo.PermissionTreeNodeVO;
import com.zhongjitang.user.mapper.UserPermissionMapper;
import com.zhongjitang.user.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements IPermissionService {

    private final UserPermissionMapper permissionMapper;

    @Override
    public R<List<PermissionTreeNodeVO>> tree() {
        LambdaQueryWrapper<UserPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPermissionDO::getStatus, 1)
                .orderByAsc(UserPermissionDO::getSortOrder);
        List<UserPermissionDO> allPermissions = permissionMapper.selectList(wrapper);
        List<PermissionTreeNodeVO> tree = buildTree(allPermissions);
        return R.ok(tree);
    }

    @Override
    public R<Void> create(PermissionCreateRequest request) {
        // 检查权限编码是否重复
        LambdaQueryWrapper<UserPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPermissionDO::getPermissionCode, request.getPermissionCode());
        Long count = permissionMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "权限编码已存在");
        }

        UserPermissionDO permission = new UserPermissionDO();
        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionCode(request.getPermissionCode());
        permission.setResourceType(request.getResourceType());
        permission.setResourceId(request.getResourceId());
        permission.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        permission.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        permission.setStatus(1);
        permissionMapper.insert(permission);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, PermissionUpdateRequest request) {
        UserPermissionDO permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "权限不存在");
        }

        if (request.getPermissionName() != null) {
            permission.setPermissionName(request.getPermissionName());
        }
        if (request.getPermissionCode() != null) {
            permission.setPermissionCode(request.getPermissionCode());
        }
        if (request.getResourceType() != null) {
            permission.setResourceType(request.getResourceType());
        }
        if (request.getResourceId() != null) {
            permission.setResourceId(request.getResourceId());
        }
        if (request.getParentId() != null) {
            permission.setParentId(request.getParentId());
        }
        if (request.getSortOrder() != null) {
            permission.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            permission.setStatus(request.getStatus());
        }

        permissionMapper.updateById(permission);
        return R.ok();
    }

    private List<PermissionTreeNodeVO> buildTree(List<UserPermissionDO> allPermissions) {
        Map<Long, List<UserPermissionDO>> parentMap = allPermissions.stream()
                .collect(Collectors.groupingBy(p -> p.getParentId() != null ? p.getParentId() : 0L));

        List<PermissionTreeNodeVO> roots = new ArrayList<>();
        for (UserPermissionDO permission : allPermissions) {
            if (permission.getParentId() == null || permission.getParentId() == 0L) {
                roots.add(buildNode(permission, parentMap));
            }
        }
        return roots;
    }

    private PermissionTreeNodeVO buildNode(UserPermissionDO permission, Map<Long, List<UserPermissionDO>> parentMap) {
        PermissionTreeNodeVO node = new PermissionTreeNodeVO();
        node.setPermission(permission);
        List<UserPermissionDO> children = parentMap.get(permission.getId());
        if (children != null) {
            node.setChildren(children.stream()
                    .map(child -> buildNode(child, parentMap))
                    .collect(Collectors.toList()));
        } else {
            node.setChildren(new ArrayList<>());
        }
        return node;
    }
}
