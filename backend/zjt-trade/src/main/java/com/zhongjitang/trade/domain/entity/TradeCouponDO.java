package com.zhongjitang.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_coupon")
public class TradeCouponDO extends BaseDO {

    private String couponName;

    /** 优惠券类型: 1满减 2折扣 3体验 */
    private Integer couponType;

    private BigDecimal discountValue;

    private BigDecimal minAmount;

    private BigDecimal maxDiscount;

    private Integer totalQuantity;

    private Integer remainingQuantity;

    private Integer perLimit;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 适用项目ID列表，逗号分隔 */
    private String applicableItems;

    /** 状态: 1启用 2禁用 */
    private Integer status;
}
