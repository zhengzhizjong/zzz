package com.zhongjitang.billing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.billing.domain.dto.TenantCreateRequest;
import com.zhongjitang.billing.domain.entity.BillingPlanDO;
import com.zhongjitang.billing.domain.entity.BillingTenantDO;
import com.zhongjitang.billing.domain.vo.TenantVO;
import com.zhongjitang.billing.mapper.BillingPlanMapper;
import com.zhongjitang.billing.mapper.BillingTenantMapper;
import com.zhongjitang.billing.service.IBillingTenantService;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class BillingTenantServiceImpl implements IBillingTenantService {

    private final BillingTenantMapper billingTenantMapper;
    private final BillingPlanMapper billingPlanMapper;

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    @Override
    public R<PageResult<BillingTenantDO>> page(Integer page, Integer pageSize, String keyword, Integer status) {
        Page<BillingTenantDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<BillingTenantDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BillingTenantDO::getTenantName, keyword)
                    .or().like(BillingTenantDO::getTenantNo, keyword)
                    .or().like(BillingTenantDO::getContactPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(BillingTenantDO::getStatus, status);
        }
        wrapper.orderByDesc(BillingTenantDO::getCreatedAt);
        Page<BillingTenantDO> result = billingTenantMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<TenantVO> getById(Long id) {
        BillingTenantDO tenant = billingTenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "租户ID: " + id);
        }
        TenantVO vo = convertToVO(tenant);
        return R.ok(vo);
    }

    @Override
    public R<Void> create(TenantCreateRequest request) {
        BillingTenantDO tenant = new BillingTenantDO();
        tenant.setTenantNo(generateTenantNo());
        tenant.setTenantName(request.getTenantName());
        tenant.setContactName(request.getContactName());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setPlanCode(request.getPlanCode());
        tenant.setBrandName(request.getBrandName());
        tenant.setStatus(1);
        tenant.setTrialExpireDate(LocalDate.now().plusDays(14));
        tenant.setWhiteLabelEnabled(0);
        if (StringUtils.hasText(request.getPlanCode())) {
            tenant.setPlanExpireDate(LocalDate.now().plusDays(30));
        }
        billingTenantMapper.insert(tenant);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, TenantCreateRequest request) {
        BillingTenantDO tenant = billingTenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "租户ID: " + id);
        }
        if (request.getTenantName() != null) {
            tenant.setTenantName(request.getTenantName());
        }
        if (request.getContactName() != null) {
            tenant.setContactName(request.getContactName());
        }
        if (request.getContactPhone() != null) {
            tenant.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            tenant.setContactEmail(request.getContactEmail());
        }
        if (request.getPlanCode() != null) {
            tenant.setPlanCode(request.getPlanCode());
        }
        if (request.getBrandName() != null) {
            tenant.setBrandName(request.getBrandName());
        }
        billingTenantMapper.updateById(tenant);
        return R.ok();
    }

    @Override
    public R<Void> updateStatus(Long id, Integer status) {
        BillingTenantDO tenant = billingTenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "租户ID: " + id);
        }
        if (status < 1 || status > 4) {
            return R.fail("状态值无效，有效值:1试用 2正式 3欠费 4停用");
        }
        tenant.setStatus(status);
        billingTenantMapper.updateById(tenant);
        return R.ok();
    }

    @Override
    public R<Void> updateWhiteLabel(Long id, String logoUrl, String primaryColor, String customDomain) {
        BillingTenantDO tenant = billingTenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "租户ID: " + id);
        }
        tenant.setWhiteLabelEnabled(1);
        if (logoUrl != null) {
            tenant.setBrandLogo(logoUrl);
        }
        if (primaryColor != null) {
            tenant.setBrandTheme(primaryColor);
        }
        if (customDomain != null) {
            tenant.setWhiteLabelConfig(customDomain);
        }
        billingTenantMapper.updateById(tenant);
        return R.ok();
    }

    private TenantVO convertToVO(BillingTenantDO tenant) {
        TenantVO vo = new TenantVO();
        vo.setId(tenant.getId());
        vo.setTenantNo(tenant.getTenantNo());
        vo.setTenantName(tenant.getTenantName());
        vo.setContactName(tenant.getContactName());
        vo.setContactPhone(tenant.getContactPhone());
        vo.setContactEmail(tenant.getContactEmail());
        vo.setStatus(tenant.getStatus());
        vo.setStatusName(getStatusName(tenant.getStatus()));
        vo.setPlanCode(tenant.getPlanCode());
        vo.setPlanExpireDate(tenant.getPlanExpireDate());
        vo.setTrialExpireDate(tenant.getTrialExpireDate());
        vo.setWhiteLabelEnabled(tenant.getWhiteLabelEnabled());
        vo.setBrandName(tenant.getBrandName());
        vo.setBrandLogo(tenant.getBrandLogo());
        vo.setBrandTheme(tenant.getBrandTheme());
        vo.setCreatedAt(tenant.getCreatedAt());

        if (StringUtils.hasText(tenant.getPlanCode())) {
            LambdaQueryWrapper<BillingPlanDO> planWrapper = new LambdaQueryWrapper<>();
            planWrapper.eq(BillingPlanDO::getPlanCode, tenant.getPlanCode());
            BillingPlanDO plan = billingPlanMapper.selectOne(planWrapper);
            if (plan != null) {
                vo.setPlanName(plan.getPlanName());
            }
        }
        return vo;
    }

    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1: return "试用";
            case 2: return "正式";
            case 3: return "欠费";
            case 4: return "停用";
            default: return "未知";
        }
    }

    private String generateTenantNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = SEQUENCE.getAndIncrement();
        if (seq > 9999) {
            SEQUENCE.set(1);
            seq = 1;
        }
        return "T" + datePart + String.format("%04d", seq);
    }
}
