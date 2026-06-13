package com.zhongjitang.trade.controller;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.entity.TradeOrderDO;
import com.zhongjitang.trade.service.ITradeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trade/orders")
@Tag(name = "订单管理")
@RequiredArgsConstructor
public class TradeOrderController {

    private final ITradeOrderService tradeOrderService;

    @GetMapping
    @Operation(summary = "订单列表")
    public R<PageResult<TradeOrderDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return tradeOrderService.page(page, pageSize, storeId, memberId, status, keyword, startDate, endDate);
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public R<TradeOrderDO> getById(@PathVariable Long id) {
        return tradeOrderService.getById(id);
    }

    @PostMapping("/from-appointment/{appointmentId}")
    @Operation(summary = "从预约创建订单")
    public R<TradeOrderDO> createFromAppointment(@PathVariable Long appointmentId) {
        return tradeOrderService.createFromAppointment(appointmentId);
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "完成服务")
    public R<Void> complete(@PathVariable Long id) {
        return tradeOrderService.complete(id);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public R<Void> cancel(@PathVariable Long id,
                          @RequestParam(required = false) String reason) {
        return tradeOrderService.cancel(id, reason);
    }
}
