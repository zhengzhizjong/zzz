package com.zhongjitang.trade.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.CouponCreateRequest;
import com.zhongjitang.trade.domain.entity.TradeCouponDO;
import com.zhongjitang.trade.service.ICouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trade/coupons")
@Tag(name = "优惠券管理")
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;

    @GetMapping
    @Operation(summary = "优惠券列表")
    public R<PageResult<TradeCouponDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer couponType,
            @RequestParam(required = false) Integer status) {
        return couponService.page(page, pageSize, couponType, status);
    }

    @PostMapping
    @Operation(summary = "创建优惠券")
    public R<Void> create(@Valid @RequestBody CouponCreateRequest request) {
        return couponService.create(request);
    }

    @PostMapping("/{couponId}/issue")
    @Operation(summary = "发放优惠券")
    public R<Void> issue(@PathVariable Long couponId,
                         @RequestBody List<Long> memberIds) {
        return couponService.issue(couponId, memberIds);
    }

    @PostMapping("/{couponId}/claim")
    @Operation(summary = "领取优惠券")
    public R<Void> claim(@PathVariable Long couponId,
                         @RequestParam Long memberId) {
        return couponService.claim(couponId, memberId);
    }
}
