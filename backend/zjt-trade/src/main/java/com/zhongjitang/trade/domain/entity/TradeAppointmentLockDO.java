package com.zhongjitang.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_appointment_lock")
public class TradeAppointmentLockDO extends BaseDO {

    private String lockId;

    private Long technicianId;

    private LocalDate lockDate;

    private String timeSlot;

    private Long memberId;

    private String sessionId;

    private LocalDateTime expireAt;

    private Integer status;
}
