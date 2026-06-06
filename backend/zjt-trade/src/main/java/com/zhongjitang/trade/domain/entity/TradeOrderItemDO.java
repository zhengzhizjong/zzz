package com.zhongjitang.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_order_item")
public class TradeOrderItemDO extends BaseDO {

    private Long orderId;

    private Long serviceItemId;

    private String serviceItemName;

    private Long technicianId;

    private String technicianName;

    @TableField("room_id")
    private Long roomId;

    @TableField("unit_price")
    private BigDecimal price;

    private Integer quantity;

    @TableField("total_amount")
    private BigDecimal subtotal;

    @TableField("discount_amount")
    private BigDecimal discountAmount;

    @TableField("service_start_time")
    private LocalDateTime serviceStartTime;

    @TableField("service_end_time")
    private LocalDateTime serviceEndTime;

    @TableField(exist = false)
    private Integer durationMinutes;
}
