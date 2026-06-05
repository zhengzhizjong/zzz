package com.zhongjitang.user.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.HealthProfileRequest;
import com.zhongjitang.user.domain.entity.UserHealthProfileDO;

public interface IHealthProfileService {

    R<UserHealthProfileDO> getByMemberId(Long memberId);

    R<Void> saveOrUpdate(HealthProfileRequest request);
}
