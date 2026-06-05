package com.zhongjitang.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.AuditLogCreateRequest;
import com.zhongjitang.system.domain.entity.SysAuditLogDO;
import com.zhongjitang.system.mapper.SysAuditLogMapper;
import com.zhongjitang.system.service.IAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements IAuditLogService {

    private final SysAuditLogMapper auditLogMapper;

    @Override
    public R<PageResult<SysAuditLogDO>> page(Integer page, Integer pageSize, String keyword, String module, String operation, LocalDate startDate, LocalDate endDate) {
        Page<SysAuditLogDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SysAuditLogDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysAuditLogDO::getUserName, keyword)
                    .or().like(SysAuditLogDO::getOperation, keyword)
                    .or().like(SysAuditLogDO::getTargetId, keyword));
        }
        if (StringUtils.hasText(module)) {
            wrapper.eq(SysAuditLogDO::getModule, module);
        }
        if (StringUtils.hasText(operation)) {
            wrapper.eq(SysAuditLogDO::getOperation, operation);
        }
        if (startDate != null) {
            wrapper.ge(SysAuditLogDO::getCreatedAt, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(SysAuditLogDO::getCreatedAt, endDate.atTime(LocalTime.MAX));
        }
        wrapper.orderByDesc(SysAuditLogDO::getCreatedAt);
        Page<SysAuditLogDO> result = auditLogMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<Void> log(AuditLogCreateRequest request) {
        SysAuditLogDO auditLog = new SysAuditLogDO();
        auditLog.setUserId(request.getUserId());
        auditLog.setUserName(request.getUserName());
        auditLog.setOperation(request.getOperation());
        auditLog.setMethod(request.getMethod());
        auditLog.setModule(request.getModule());
        auditLog.setTargetId(request.getTargetId());
        auditLog.setTargetType(request.getTargetType());
        auditLog.setOldValue(request.getOldValue());
        auditLog.setNewValue(request.getNewValue());
        auditLog.setIp(request.getIp());
        auditLog.setUserAgent(request.getUserAgent());
        auditLog.setDuration(request.getDuration());
        auditLog.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        auditLogMapper.insert(auditLog);
        return R.ok();
    }
}
