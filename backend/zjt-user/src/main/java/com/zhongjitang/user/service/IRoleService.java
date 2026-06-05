package com.zhongjitang.user.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.AssignPermissionRequest;
import com.zhongjitang.user.domain.dto.RoleCreateRequest;
import com.zhongjitang.user.domain.dto.RoleUpdateRequest;
import com.zhongjitang.user.domain.entity.UserRoleDO;

import java.util.List;

public interface IRoleService {

    R<List<UserRoleDO>> list();

    R<Void> create(RoleCreateRequest request);

    R<Void> update(Long id, RoleUpdateRequest request);

    R<Void> assignPermissions(Long id, AssignPermissionRequest request);

    R<Void> delete(Long id);
}
