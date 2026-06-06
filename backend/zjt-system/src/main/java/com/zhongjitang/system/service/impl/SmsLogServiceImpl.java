package com.zhongjitang.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.dto.SmsLogCreateRequest;
import com.zhongjitang.system.domain.entity.SysSmsLogDO;
import com.zhongjitang.system.mapper.SysSmsLogMapper;
import com.zhongjitang.system.service.ISmsLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class SmsLogServiceImpl implements ISmsLogService {

    private final SysSmsLogMapper smsLogMapper;

    @Override
    public R<PageResult<SysSmsLogDO>> page(Integer page, Integer pageSize, String phone, Integer status, LocalDate startDate, LocalDate endDate) {
        Page<SysSmsLogDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SysSmsLogDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(phone)) {
            wrapper.like(SysSmsLogDO::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(SysSmsLogDO::getSendStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(SysSmsLogDO::getCreatedAt, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(SysSmsLogDO::getCreatedAt, endDate.atTime(LocalTime.MAX));
        }
        wrapper.orderByDesc(SysSmsLogDO::getCreatedAt);
        Page<SysSmsLogDO> result = smsLogMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<Void> log(SmsLogCreateRequest request) {
        SysSmsLogDO smsLog = new SysSmsLogDO();
        smsLog.setPhone(request.getPhone());
        smsLog.setTemplateCode(request.getTemplateCode());
        smsLog.setTemplateParams(request.getTemplateParams());
        smsLog.setContent(request.getContent());
        smsLog.setSendStatus(request.getSendStatus() != null ? request.getSendStatus() : 1); // 默认发送中
        smsLog.setSendResult(request.getSendResult());
        smsLog.setBizType(request.getBizType());
        smsLog.setBizId(request.getBizId());
        smsLogMapper.insert(smsLog);
        return R.ok();
    }

    @Override
    public R<SysSmsLogDO> getById(Long id) {
        SysSmsLogDO smsLog = smsLogMapper.selectById(id);
        if (smsLog == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "短信日志ID: " + id);
        }
        return R.ok(smsLog);
    }
}
