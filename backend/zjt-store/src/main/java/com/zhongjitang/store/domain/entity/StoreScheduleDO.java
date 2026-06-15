package com.zhongjitang.store.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store_schedule")
public class StoreScheduleDO extends BaseDO {

    @TableField("employee_id")
    private Long technicianId;

    private LocalDate scheduleDate;

    private LocalTime startTime;

    private LocalTime endTime;

    /** 排班类型: 1上班 2休息 3请假 */
    private Integer scheduleType;

    /** 班次: morning早班, afternoon中班, evening晚班 */
    private String shiftType;

    @TableField(exist = false)
    private String notes;
}
