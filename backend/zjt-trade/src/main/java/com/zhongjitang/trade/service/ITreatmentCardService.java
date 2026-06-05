package com.zhongjitang.trade.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.TreatmentCardCreateRequest;
import com.zhongjitang.trade.domain.entity.TradeTreatmentCardDO;
import com.zhongjitang.trade.domain.entity.TradeTreatmentCardUsageDO;

import java.util.List;

public interface ITreatmentCardService {

    R<PageResult<TradeTreatmentCardDO>> page(Integer page, Integer pageSize, Long memberId, Integer status);

    R<Void> create(TreatmentCardCreateRequest request);

    R<Void> use(Long cardId, Long appointmentId);

    R<List<TradeTreatmentCardUsageDO>> getUsageHistory(Long cardId);

    R<List<TradeTreatmentCardDO>> myCards(Long memberId);
}
