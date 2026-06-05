package com.zhongjitang.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.integration.domain.dto.LeadCreateRequest;
import com.zhongjitang.integration.domain.dto.LeadUpdateRequest;
import com.zhongjitang.integration.domain.entity.IntegrationLeadDO;
import com.zhongjitang.integration.mapper.IntegrationLeadMapper;
import com.zhongjitang.integration.service.ILeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements ILeadService {

    private final IntegrationLeadMapper integrationLeadMapper;

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    @Override
    public R<PageResult<IntegrationLeadDO>> page(Integer page, Integer pageSize, String keyword, String platform, Integer status) {
        Page<IntegrationLeadDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<IntegrationLeadDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(IntegrationLeadDO::getCustomerName, keyword)
                    .or().like(IntegrationLeadDO::getCustomerPhone, keyword)
                    .or().like(IntegrationLeadDO::getLeadNo, keyword));
        }
        if (StringUtils.hasText(platform)) {
            wrapper.eq(IntegrationLeadDO::getPlatform, platform);
        }
        if (status != null) {
            wrapper.eq(IntegrationLeadDO::getFollowStatus, status);
        }
        wrapper.orderByDesc(IntegrationLeadDO::getCreatedAt);
        Page<IntegrationLeadDO> result = integrationLeadMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<IntegrationLeadDO> getById(Long id) {
        IntegrationLeadDO lead = integrationLeadMapper.selectById(id);
        if (lead == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "线索ID: " + id);
        }
        return R.ok(lead);
    }

    @Override
    public R<Void> create(LeadCreateRequest request) {
        IntegrationLeadDO lead = new IntegrationLeadDO();
        lead.setLeadNo(generateLeadNo());
        lead.setPlatform(request.getPlatform());
        lead.setPlatformLeadId(request.getPlatformLeadId());
        lead.setSourceChannel(request.getSourceChannel());
        lead.setCampaignId(request.getCampaignId());
        lead.setReferrerName(request.getReferrerName());
        lead.setCustomerName(request.getCustomerName());
        lead.setCustomerPhone(request.getCustomerPhone());
        lead.setCustomerGender(request.getCustomerGender());
        lead.setCustomerAge(request.getCustomerAge());
        lead.setCustomerIntent(request.getCustomerIntent());
        lead.setConsultationContent(request.getConsultationContent());
        lead.setLeadClass(request.getLeadClass());
        lead.setFollowStatus(1);
        integrationLeadMapper.insert(lead);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, LeadUpdateRequest request) {
        IntegrationLeadDO lead = integrationLeadMapper.selectById(id);
        if (lead == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "线索ID: " + id);
        }
        if (request.getCustomerName() != null) {
            lead.setCustomerName(request.getCustomerName());
        }
        if (request.getCustomerPhone() != null) {
            lead.setCustomerPhone(request.getCustomerPhone());
        }
        if (request.getCustomerGender() != null) {
            lead.setCustomerGender(request.getCustomerGender());
        }
        if (request.getCustomerAge() != null) {
            lead.setCustomerAge(request.getCustomerAge());
        }
        if (request.getCustomerIntent() != null) {
            lead.setCustomerIntent(request.getCustomerIntent());
        }
        if (request.getConsultationContent() != null) {
            lead.setConsultationContent(request.getConsultationContent());
        }
        if (request.getLeadClass() != null) {
            lead.setLeadClass(request.getLeadClass());
        }
        if (request.getFollowStatus() != null) {
            lead.setFollowStatus(request.getFollowStatus());
        }
        if (request.getAiSummary() != null) {
            lead.setAiSummary(request.getAiSummary());
        }
        integrationLeadMapper.updateById(lead);
        return R.ok();
    }

    @Override
    public R<Void> assign(Long id, Long assignedTo) {
        IntegrationLeadDO lead = integrationLeadMapper.selectById(id);
        if (lead == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "线索ID: " + id);
        }
        lead.setAssignedTo(assignedTo);
        lead.setAssignedAt(LocalDateTime.now());
        lead.setFollowStatus(2);
        integrationLeadMapper.updateById(lead);
        return R.ok();
    }

    @Override
    public R<Void> convert(Long id, Long memberId) {
        IntegrationLeadDO lead = integrationLeadMapper.selectById(id);
        if (lead == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "线索ID: " + id);
        }
        lead.setFollowStatus(3);
        lead.setConvertedOrderId(memberId);
        lead.setConvertedAt(LocalDateTime.now());
        integrationLeadMapper.updateById(lead);
        return R.ok();
    }

    @Override
    public R<Void> updateStatus(Long id, Integer status) {
        IntegrationLeadDO lead = integrationLeadMapper.selectById(id);
        if (lead == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "线索ID: " + id);
        }
        if (status < 1 || status > 4) {
            return R.fail("状态值无效，有效值:1待跟进 2跟进中 3已转化 4无效");
        }
        lead.setFollowStatus(status);
        integrationLeadMapper.updateById(lead);
        return R.ok();
    }

    private String generateLeadNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = SEQUENCE.getAndIncrement();
        if (seq > 9999) {
            SEQUENCE.set(1);
            seq = 1;
        }
        return "L" + datePart + String.format("%04d", seq);
    }
}
