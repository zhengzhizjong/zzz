package com.zhongjitang.trade.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.dto.RefundApplyRequest;
import com.zhongjitang.trade.domain.entity.TradeRefundDO;
import com.zhongjitang.trade.service.IRefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/trade/refunds")
@Tag(name = "退款管理")
@RequiredArgsConstructor
public class RefundController {

    private final IRefundService refundService;

    @PostMapping
    @Operation(summary = "申请退款")
    public R<TradeRefundDO> apply(@Valid @RequestBody RefundApplyRequest request) {
        return refundService.apply(request);
    }

    @GetMapping
    @Operation(summary = "退款列表")
    public R<PageResult<TradeRefundDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Integer status) {
        return refundService.page(page, pageSize, orderId, memberId, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "退款详情")
    public R<TradeRefundDO> getById(@PathVariable Long id) {
        return refundService.getById(id);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审批通过")
    public R<Void> approve(@PathVariable Long id) {
        return refundService.approve(id);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "审批拒绝")
    public R<Void> reject(@PathVariable Long id,
                          @RequestParam String rejectReason) {
        return refundService.reject(id, rejectReason);
    }
}
