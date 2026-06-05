package com.zhongjitang.store.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store_room")
public class StoreRoomDO extends BaseDO {

    private Long storeId;

    private String roomNo;

    private String roomName;

    /** 房间类型: 1普通 2VIP 3套间 */
    private Integer roomType;

    private Integer floor;

    private Integer capacity;

    /** 状态: 1空闲 2使用中 3维护中 */
    private Integer status;

    private String equipment;
}
