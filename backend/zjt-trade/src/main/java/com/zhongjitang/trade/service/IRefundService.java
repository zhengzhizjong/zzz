package com.zhongjitang.trade.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.RefundApplyRequest;
import com.zhongjitang.trade.domain.entity.TradeRefundDO;

public interface IRefundService {

    R<TradeRefundDO> apply(RefundApplyRequest request);

    R<Void> approve(Long id);

    R<Void> reject(Long id, String rejectReason);

    R<TradeRefundDO> getById(Long id);

    R<PageResult<TradeRefundDO>> page(Integer page, Integer pageSize, Long orderId, Long memberId, Integer status);
}
