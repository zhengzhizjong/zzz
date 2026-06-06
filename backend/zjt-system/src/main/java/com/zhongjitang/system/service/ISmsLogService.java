package com.zhongjitang.system.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.SmsLogCreateRequest;
import com.zhongjitang.system.domain.entity.SysSmsLogDO;

import java.time.LocalDate;

public interface ISmsLogService {

    R<PageResult<SysSmsLogDO>> page(Integer page, Integer pageSize, String phone, Integer status, LocalDate startDate, LocalDate endDate);

    R<Void> log(SmsLogCreateRequest request);

    R<SysSmsLogDO> getById(Long id);
}
