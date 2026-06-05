package com.zhongjitang.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.TreatmentCardCreateRequest;
import com.zhongjitang.trade.domain.entity.TradeTreatmentCardDO;
import com.zhongjitang.trade.domain.entity.TradeTreatmentCardUsageDO;
import com.zhongjitang.trade.mapper.TradeTreatmentCardMapper;
import com.zhongjitang.trade.mapper.TradeTreatmentCardUsageMapper;
import com.zhongjitang.trade.service.ITreatmentCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TreatmentCardServiceImpl implements ITreatmentCardService {

    private final TradeTreatmentCardMapper tradeTreatmentCardMapper;
    private final TradeTreatmentCardUsageMapper tradeTreatmentCardUsageMapper;

    @Override
    public R<PageResult<TradeTreatmentCardDO>> page(Integer page, Integer pageSize, Long memberId, Integer status) {
        Page<TradeTreatmentCardDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TradeTreatmentCardDO> wrapper = new LambdaQueryWrapper<>();
        if (memberId != null) {
            wrapper.eq(TradeTreatmentCardDO::getMemberId, memberId);
        }
        if (status != null) {
            wrapper.eq(TradeTreatmentCardDO::getStatus, status);
        }
        wrapper.orderByDesc(TradeTreatmentCardDO::getCreatedAt);
        Page<TradeTreatmentCardDO> result = tradeTreatmentCardMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> create(TreatmentCardCreateRequest request) {
        TradeTreatmentCardDO card = new TradeTreatmentCardDO();
        card.setCardName(request.getCardName());
        card.setServiceItemId(request.getServiceItemId());
        card.setTotalSessions(request.getTotalSessions());
        card.setRemainingSessions(request.getTotalSessions());
        card.setMemberId(request.getMemberId());
        card.setOrderId(request.getOrderId());
        card.setPrice(request.getPrice());
        card.setStatus(1); // 有效
        card.setExpireAt(request.getExpireAt());

        tradeTreatmentCardMapper.insert(card);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> use(Long cardId, Long appointmentId) {
        TradeTreatmentCardDO card = tradeTreatmentCardMapper.selectById(cardId);
        if (card == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "疗程卡不存在，ID: " + cardId);
        }

        // 校验状态
        if (card.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "疗程卡已失效");
        }

        // 校验过期
        if (card.getExpireAt() != null && LocalDateTime.now().isAfter(card.getExpireAt())) {
            card.setStatus(3); // 已过期
            tradeTreatmentCardMapper.updateById(card);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "疗程卡已过期");
        }

        // 校验剩余次数
        if (card.getRemainingSessions() <= 0) {
            card.setStatus(2); // 已用完
            tradeTreatmentCardMapper.updateById(card);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "疗程卡次数已用完");
        }

        // 扣减次数
        card.setRemainingSessions(card.getRemainingSessions() - 1);
        if (card.getRemainingSessions() <= 0) {
            card.setStatus(2); // 已用完
        }
        tradeTreatmentCardMapper.updateById(card);

        // 记录使用记录
        TradeTreatmentCardUsageDO usage = new TradeTreatmentCardUsageDO();
        usage.setCardId(cardId);
        usage.setAppointmentId(appointmentId);
        usage.setUsedAt(LocalDateTime.now());
        tradeTreatmentCardUsageMapper.insert(usage);

        return R.ok();
    }

    @Override
    public R<List<TradeTreatmentCardUsageDO>> getUsageHistory(Long cardId) {
        LambdaQueryWrapper<TradeTreatmentCardUsageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeTreatmentCardUsageDO::getCardId, cardId)
                .orderByDesc(TradeTreatmentCardUsageDO::getUsedAt);
        List<TradeTreatmentCardUsageDO> usageList = tradeTreatmentCardUsageMapper.selectList(wrapper);
        return R.ok(usageList);
    }

    @Override
    public R<List<TradeTreatmentCardDO>> myCards(Long memberId) {
        LambdaQueryWrapper<TradeTreatmentCardDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeTreatmentCardDO::getMemberId, memberId)
                .orderByDesc(TradeTreatmentCardDO::getCreatedAt);
        List<TradeTreatmentCardDO> cards = tradeTreatmentCardMapper.selectList(wrapper);
        return R.ok(cards);
    }
}
