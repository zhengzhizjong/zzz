package com.zhongjitang.system.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.AuditLogCreateRequest;
import com.zhongjitang.system.domain.entity.SysAuditLogDO;

import java.time.LocalDate;

public interface IAuditLogService {

    R<PageResult<SysAuditLogDO>> page(Integer page, Integer pageSize, String keyword, String module, String operation, LocalDate startDate, LocalDate endDate);

    R<Void> log(AuditLogCreateRequest request);
}
