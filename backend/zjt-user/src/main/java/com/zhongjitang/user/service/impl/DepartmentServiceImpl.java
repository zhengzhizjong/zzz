package com.zhongjitang.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.DepartmentCreateRequest;
import com.zhongjitang.user.domain.dto.DepartmentUpdateRequest;
import com.zhongjitang.user.domain.entity.UserDepartmentDO;
import com.zhongjitang.user.domain.vo.DepartmentTreeNodeVO;
import com.zhongjitang.user.mapper.UserDepartmentMapper;
import com.zhongjitang.user.service.IDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements IDepartmentService {

    private final UserDepartmentMapper departmentMapper;

    @Override
    public R<List<DepartmentTreeNodeVO>> tree() {
        LambdaQueryWrapper<UserDepartmentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(UserDepartmentDO::getSortOrder);
        List<UserDepartmentDO> allDepts = departmentMapper.selectList(wrapper);
        List<DepartmentTreeNodeVO> tree = buildTree(allDepts);
        return R.ok(tree);
    }

    @Override
    public R<Void> create(DepartmentCreateRequest request) {
        UserDepartmentDO dept = new UserDepartmentDO();
        dept.setDeptName(request.getDeptName());
        dept.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        dept.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        dept.setStatus(1);
        departmentMapper.insert(dept);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, DepartmentUpdateRequest request) {
        UserDepartmentDO dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "部门不存在");
        }

        if (request.getDeptName() != null) {
            dept.setDeptName(request.getDeptName());
        }
        if (request.getParentId() != null) {
            dept.setParentId(request.getParentId());
        }
        if (request.getSortOrder() != null) {
            dept.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            dept.setStatus(request.getStatus());
        }

        departmentMapper.updateById(dept);
        return R.ok();
    }

    private List<DepartmentTreeNodeVO> buildTree(List<UserDepartmentDO> allDepts) {
        Map<Long, List<UserDepartmentDO>> parentMap = allDepts.stream()
                .collect(Collectors.groupingBy(d -> d.getParentId() != null ? d.getParentId() : 0L));

        List<DepartmentTreeNodeVO> roots = new ArrayList<>();
        for (UserDepartmentDO dept : allDepts) {
            if (dept.getParentId() == null || dept.getParentId() == 0L) {
                roots.add(buildNode(dept, parentMap));
            }
        }
        return roots;
    }

    private DepartmentTreeNodeVO buildNode(UserDepartmentDO dept, Map<Long, List<UserDepartmentDO>> parentMap) {
        DepartmentTreeNodeVO node = new DepartmentTreeNodeVO();
        node.setDepartment(dept);
        List<UserDepartmentDO> children = parentMap.get(dept.getId());
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
