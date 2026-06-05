package com.zhongjitang.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_appointment")
public class TradeAppointmentDO extends BaseDO {

    private String appointmentNo;

    private Long memberId;

    private String memberName;

    private String memberPhone;

    private Long serviceItemId;

    private String serviceItemName;

    private Long technicianId;

    private String technicianName;

    private Long roomId;

    private LocalDate appointmentDate;

    private String timeSlot;

    private LocalTime appointmentTime;

    private Integer durationMinutes;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Version
    private Integer version;

    private Integer status;

    private String modifyReason;

    private String cancelReason;

    private Integer sourceType;

    private String sourceChannel;

    private String consultationNotes;
}
