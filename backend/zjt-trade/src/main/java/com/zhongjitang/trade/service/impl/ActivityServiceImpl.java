package com.zhongjitang.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.ActivityCreateRequest;
import com.zhongjitang.trade.domain.dto.ActivityUpdateRequest;
import com.zhongjitang.trade.domain.entity.TradeActivityDO;
import com.zhongjitang.trade.mapper.TradeActivityMapper;
import com.zhongjitang.trade.service.IActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements IActivityService {

    private final TradeActivityMapper tradeActivityMapper;

    @Override
    public R<PageResult<TradeActivityDO>> page(Integer page, Integer pageSize, String keyword, Integer status) {
        Page<TradeActivityDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TradeActivityDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(TradeActivityDO::getActivityName, keyword);
        }
        if (status != null) {
            wrapper.eq(TradeActivityDO::getStatus, status);
        }
        wrapper.orderByDesc(TradeActivityDO::getCreatedAt);
        Page<TradeActivityDO> result = tradeActivityMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    public R<TradeActivityDO> getById(Long id) {
        TradeActivityDO activity = tradeActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动ID: " + id);
        }
        return R.ok(activity);
    }

    @Override
    public R<Void> create(ActivityCreateRequest request) {
        TradeActivityDO activity = new TradeActivityDO();
        activity.setActivityName(request.getActivityName());
        activity.setActivityType(request.getActivityType());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setRulesJson(request.getRulesJson());
        activity.setDescription(request.getDescription());
        activity.setCoverImageUrl(request.getCoverImageUrl());
        activity.setApplicableStores(request.getApplicableStores());
        activity.setStatus(1); // 默认草稿
        tradeActivityMapper.insert(activity);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, ActivityUpdateRequest request) {
        TradeActivityDO activity = tradeActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动ID: " + id);
        }
        if (StringUtils.hasText(request.getActivityName())) {
            activity.setActivityName(request.getActivityName());
        }
        if (request.getActivityType() != null) {
            activity.setActivityType(request.getActivityType());
        }
        if (request.getStartTime() != null) {
            activity.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            activity.setEndTime(request.getEndTime());
        }
        if (request.getRulesJson() != null) {
            activity.setRulesJson(request.getRulesJson());
        }
        if (request.getDescription() != null) {
            activity.setDescription(request.getDescription());
        }
        if (request.getCoverImageUrl() != null) {
            activity.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getApplicableStores() != null) {
            activity.setApplicableStores(request.getApplicableStores());
        }
        tradeActivityMapper.updateById(activity);
        return R.ok();
    }

    @Override
    public R<Void> updateStatus(Long id, Integer status) {
        TradeActivityDO activity = tradeActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动ID: " + id);
        }
        if (status < 1 || status > 4) {
            return R.fail("状态值无效，有效值: 1草稿 2进行中 3已结束 4已停用");
        }
        activity.setStatus(status);
        tradeActivityMapper.updateById(activity);
        return R.ok();
    }

    @Override
    public R<List<TradeActivityDO>> getActiveList() {
        LambdaQueryWrapper<TradeActivityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeActivityDO::getStatus, 2); // 进行中
        wrapper.le(TradeActivityDO::getStartTime, LocalDateTime.now());
        wrapper.ge(TradeActivityDO::getEndTime, LocalDateTime.now());
        wrapper.orderByDesc(TradeActivityDO::getCreatedAt);
        List<TradeActivityDO> list = tradeActivityMapper.selectList(wrapper);
        return R.ok(list);
    }
}
