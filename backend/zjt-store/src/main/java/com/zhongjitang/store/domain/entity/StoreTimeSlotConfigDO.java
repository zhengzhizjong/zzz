package com.zhongjitang.store.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store_time_slot_config")
public class StoreTimeSlotConfigDO extends BaseDO {

    private Long storeId;

    private LocalTime businessStartTime;

    private LocalTime businessEndTime;

    private Integer slotDurationMinutes;

    private LocalTime restStartTime;

    private LocalTime restEndTime;

    private Integer status;
}
