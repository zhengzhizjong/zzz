package com.zhongjitang.user.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.PermissionCreateRequest;
import com.zhongjitang.user.domain.dto.PermissionUpdateRequest;
import com.zhongjitang.user.domain.vo.PermissionTreeNodeVO;

import java.util.List;

public interface IPermissionService {

    R<List<PermissionTreeNodeVO>> tree();

    R<Void> create(PermissionCreateRequest request);

    R<Void> update(Long id, PermissionUpdateRequest request);
}
