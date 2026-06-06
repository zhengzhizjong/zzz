package com.zhongjitang.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_treatment_card_usage")
public class TradeTreatmentCardUsageDO extends BaseDO {

    private Long cardId;

    @TableField(exist = false)
    private Long appointmentId;

    private Long orderId;

    private Integer usedUses;

    private LocalDateTime usedAt;

    @TableField(exist = false)
    private String notes;
}
