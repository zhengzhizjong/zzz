package com.zhongjitang.trade.service;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.LockTempRequest;
import com.zhongjitang.trade.domain.vo.LockResultVO;

public interface AppointmentLockService {

    R<LockResultVO> lockTemp(LockTempRequest request, Long memberId);

    void releaseLock(String lockId);
}
