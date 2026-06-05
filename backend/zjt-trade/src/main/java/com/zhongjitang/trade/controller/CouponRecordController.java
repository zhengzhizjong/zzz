package com.zhongjitang.trade.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.entity.TradeCouponRecordDO;
import com.zhongjitang.trade.service.ICouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trade/coupon-records")
@Tag(name = "优惠券记录")
@RequiredArgsConstructor
public class CouponRecordController {

    private final ICouponService couponService;

    @GetMapping("/my")
    @Operation(summary = "我的优惠券列表")
    public R<List<TradeCouponRecordDO>> myCoupons(
            @RequestParam Long memberId,
            @RequestParam(required = false) Integer status) {
        return couponService.myCoupons(memberId, status);
    }

    @PutMapping("/{recordId}/use")
    @Operation(summary = "使用优惠券")
    public R<Void> use(@PathVariable Long recordId,
                       @RequestParam Long orderId) {
        return couponService.use(recordId, orderId);
    }
}
