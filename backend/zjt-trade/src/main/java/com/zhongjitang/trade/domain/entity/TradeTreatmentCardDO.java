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
@TableName("trade_treatment_card")
public class TradeTreatmentCardDO extends BaseDO {

    private String cardNo;

    private String cardName;

    @TableField(exist = false)
    private Long serviceItemId;

    @TableField("total_uses")
    private Integer totalSessions;

    @TableField("remaining_uses")
    private Integer remainingSessions;

    private Long memberId;

    @TableField(exist = false)
    private Long orderId;

    @TableField("purchase_amount")
    private BigDecimal price;

    /** 状态: 1有效 2已用完 3已过期 */
    private Integer status;

    @TableField("expire_time")
    private LocalDateTime expireAt;
}
