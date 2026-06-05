package com.zhongjitang.trade.domain.entity;

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

    private String cardName;

    private Long serviceItemId;

    private Integer totalSessions;

    private Integer remainingSessions;

    private Long memberId;

    private Long orderId;

    private BigDecimal price;

    /** 状态: 1有效 2已用完 3已过期 */
    private Integer status;

    private LocalDateTime expireAt;
}
