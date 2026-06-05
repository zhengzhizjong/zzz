package com.zhongjitang.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_order_item")
public class TradeOrderItemDO extends BaseDO {

    private Long orderId;

    private Long serviceItemId;

    private String serviceItemName;

    private Long technicianId;

    private String technicianName;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;

    private Integer durationMinutes;
}
