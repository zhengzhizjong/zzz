package com.zhongjitang.billing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.billing.domain.dto.BillingPlanCreateRequest;
import com.zhongjitang.billing.domain.entity.BillingPlanDO;
import com.zhongjitang.billing.mapper.BillingPlanMapper;
import com.zhongjitang.billing.service.IBillingPlanService;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingPlanServiceImpl implements IBillingPlanService {

    private final BillingPlanMapper billingPlanMapper;

    @Override
    public R<List<BillingPlanDO>> list() {
        LambdaQueryWrapper<BillingPlanDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingPlanDO::getStatus, 1);
        wrapper.orderByAsc(BillingPlanDO::getSortOrder);
        List<BillingPlanDO> list = billingPlanMapper.selectList(wrapper);
        return R.ok(list);
    }

    @Override
    public R<BillingPlanDO> getById(Long id) {
        BillingPlanDO plan = billingPlanMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐ID: " + id);
        }
        return R.ok(plan);
    }

    @Override
    public R<Void> create(BillingPlanCreateRequest request) {
        LambdaQueryWrapper<BillingPlanDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingPlanDO::getPlanCode, request.getPlanCode());
        if (billingPlanMapper.selectCount(wrapper) > 0) {
            return R.fail("套餐编码已存在: " + request.getPlanCode());
        }

        BillingPlanDO plan = new BillingPlanDO();
        plan.setPlanCode(request.getPlanCode());
        plan.setPlanName(request.getPlanName());
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setYearlyPrice(request.getYearlyPrice());
        plan.setMaxStores(request.getMaxStores());
        plan.setFeaturesJson(request.getFeaturesJson());
        plan.setDescription(request.getDescription());
        plan.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        plan.setStatus(1);
        billingPlanMapper.insert(plan);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, BillingPlanCreateRequest request) {
        BillingPlanDO plan = billingPlanMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐ID: " + id);
        }
        if (request.getPlanName() != null) {
            plan.setPlanName(request.getPlanName());
        }
        if (request.getMonthlyPrice() != null) {
            plan.setMonthlyPrice(request.getMonthlyPrice());
        }
        if (request.getYearlyPrice() != null) {
            plan.setYearlyPrice(request.getYearlyPrice());
        }
        if (request.getMaxStores() != null) {
            plan.setMaxStores(request.getMaxStores());
        }
        if (request.getFeaturesJson() != null) {
            plan.setFeaturesJson(request.getFeaturesJson());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            plan.setSortOrder(request.getSortOrder());
        }
        billingPlanMapper.updateById(plan);
        return R.ok();
    }
}
