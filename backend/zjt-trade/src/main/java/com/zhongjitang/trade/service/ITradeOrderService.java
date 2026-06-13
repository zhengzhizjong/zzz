package com.zhongjitang.trade.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.entity.TradeOrderDO;

public interface ITradeOrderService {

    R<PageResult<TradeOrderDO>> page(Integer page, Integer pageSize, Long storeId, Long memberId, Integer status, String keyword, String startDate, String endDate);

    R<TradeOrderDO> getById(Long id);

    R<TradeOrderDO> createFromAppointment(Long appointmentId);

    R<Void> complete(Long id);

    R<Void> cancel(Long id, String reason);
}
