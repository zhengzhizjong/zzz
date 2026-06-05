package com.zhongjitang.user.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.DepartmentCreateRequest;
import com.zhongjitang.user.domain.dto.DepartmentUpdateRequest;
import com.zhongjitang.user.domain.vo.DepartmentTreeNodeVO;

import java.util.List;

public interface IDepartmentService {

    R<List<DepartmentTreeNodeVO>> tree();

    R<Void> create(DepartmentCreateRequest request);

    R<Void> update(Long id, DepartmentUpdateRequest request);
}
