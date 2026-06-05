package com.zhongjitang.trade.domain.vo;

import com.zhongjitang.trade.domain.entity.TradeOrderDO;
import com.zhongjitang.trade.domain.entity.TradeOrderItemDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "订单详情VO")
public class OrderVO {

    @Schema(description = "订单信息")
    private TradeOrderDO order;

    @Schema(description = "订单明细列表")
    private List<TradeOrderItemDO> items;

    @Schema(description = "支付信息")
    private PaymentVO payment;
}
