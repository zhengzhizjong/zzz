package com.zhongjitang.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.MemberLevelCreateRequest;
import com.zhongjitang.user.domain.dto.MemberLevelUpdateRequest;
import com.zhongjitang.user.domain.entity.UserMemberLevelDO;
import com.zhongjitang.user.mapper.UserMemberLevelMapper;
import com.zhongjitang.user.service.IMemberLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberLevelServiceImpl implements IMemberLevelService {

    private final UserMemberLevelMapper memberLevelMapper;

    @Override
    public R<List<UserMemberLevelDO>> list() {
        LambdaQueryWrapper<UserMemberLevelDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMemberLevelDO::getStatus, 1)
                .orderByAsc(UserMemberLevelDO::getSortOrder);
        List<UserMemberLevelDO> list = memberLevelMapper.selectList(wrapper);
        return R.ok(list);
    }

    @Override
    public R<Void> create(MemberLevelCreateRequest request) {
        // 检查等级编码是否重复
        LambdaQueryWrapper<UserMemberLevelDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMemberLevelDO::getLevelCode, request.getLevelCode());
        Long count = memberLevelMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "等级编码已存在");
        }

        UserMemberLevelDO level = new UserMemberLevelDO();
        level.setLevelName(request.getLevelName());
        level.setLevelCode(request.getLevelCode());
        level.setMinSpent(request.getMinSpent());
        level.setMinVisits(request.getMinVisits());
        level.setDiscountRate(request.getDiscountRate());
        level.setIconUrl(request.getIconUrl());
        level.setColor(request.getColor());
        level.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        level.setStatus(1);
        memberLevelMapper.insert(level);
        return R.ok();
    }

    @Override
    public R<Void> update(Long id, MemberLevelUpdateRequest request) {
        UserMemberLevelDO level = memberLevelMapper.selectById(id);
        if (level == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "等级不存在");
        }

        if (request.getLevelName() != null) {
            level.setLevelName(request.getLevelName());
        }
        if (request.getLevelCode() != null) {
            level.setLevelCode(request.getLevelCode());
        }
        if (request.getMinSpent() != null) {
            level.setMinSpent(request.getMinSpent());
        }
        if (request.getMinVisits() != null) {
            level.setMinVisits(request.getMinVisits());
        }
        if (request.getDiscountRate() != null) {
            level.setDiscountRate(request.getDiscountRate());
        }
        if (request.getIconUrl() != null) {
            level.setIconUrl(request.getIconUrl());
        }
        if (request.getColor() != null) {
            level.setColor(request.getColor());
        }
        if (request.getSortOrder() != null) {
            level.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            level.setStatus(request.getStatus());
        }

        memberLevelMapper.updateById(level);
        return R.ok();
    }
}
