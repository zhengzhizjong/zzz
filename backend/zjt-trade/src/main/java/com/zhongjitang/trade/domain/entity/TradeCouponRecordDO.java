package com.zhongjitang.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_coupon_record")
public class TradeCouponRecordDO extends BaseDO {

    private Long couponId;

    private Long memberId;

    private Long orderId;

    /** 状态: 1未使用 2已使用 3已过期 */
    private Integer status;

    @TableField("used_time")
    private LocalDateTime usedAt;

    @TableField(exist = false)
    private LocalDateTime expireAt;
}
