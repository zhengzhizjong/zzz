package com.zhongjitang.store.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store_room")
public class StoreRoomDO extends BaseDO {

    private String roomNo;

    private String roomName;

    /** 房间类型: 1普通 2VIP 3套间 */
    private Integer roomType;

    @TableField(exist = false)
    private Integer floor;

    @TableField(exist = false)
    private Integer capacity;

    /** 状态: 1空闲 2使用中 3维护中 */
    private Integer status;

    @TableField(exist = false)
    private String equipment;
}
