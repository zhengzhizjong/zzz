package com.zhongjitang.trade.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.CouponCreateRequest;
import com.zhongjitang.trade.domain.entity.TradeCouponDO;
import com.zhongjitang.trade.domain.entity.TradeCouponRecordDO;

import java.util.List;

public interface ICouponService {

    R<PageResult<TradeCouponDO>> page(Integer page, Integer pageSize, Integer couponType, Integer status);

    R<Void> create(CouponCreateRequest request);

    R<Void> issue(Long couponId, List<Long> memberIds);

    R<Void> claim(Long couponId, Long memberId);

    R<List<TradeCouponRecordDO>> myCoupons(Long memberId, Integer status);

    R<Void> use(Long recordId, Long orderId);
}
