package com.zhongjitang.trade.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.trade.domain.vo.PaymentVO;
import com.zhongjitang.trade.service.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trade/payments")
@Tag(name = "支付管理")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/create")
    @Operation(summary = "创建支付单")
    public R<PaymentVO> createPayment(
            @RequestParam Long orderId,
            @RequestParam String channel) {
        return paymentService.createPayment(orderId, channel);
    }

    @PostMapping("/wechat-notify")
    @Operation(summary = "微信支付回调")
    public R<Void> handleWechatNotify(@RequestBody String notifyData) {
        return paymentService.handleWechatNotify(notifyData);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "按订单查询支付")
    public R<PaymentVO> getByOrderId(@PathVariable Long orderId) {
        return paymentService.getByOrderId(orderId);
    }
}
