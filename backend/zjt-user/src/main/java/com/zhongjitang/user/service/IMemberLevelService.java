package com.zhongjitang.user.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.MemberLevelCreateRequest;
import com.zhongjitang.user.domain.dto.MemberLevelUpdateRequest;
import com.zhongjitang.user.domain.entity.UserMemberLevelDO;

import java.util.List;

public interface IMemberLevelService {

    R<List<UserMemberLevelDO>> list();

    R<Void> create(MemberLevelCreateRequest request);

    R<Void> update(Long id, MemberLevelUpdateRequest request);
}
