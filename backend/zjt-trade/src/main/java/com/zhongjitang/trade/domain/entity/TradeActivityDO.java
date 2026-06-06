package com.zhongjitang.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_activity")
public class TradeActivityDO extends BaseDO {

    /** 活动名称 */
    private String activityName;

    /** 活动类型: 1满减 2折扣 3赠品 4体验 */
    private Integer activityType;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 规则配置(JSON) */
    private String rulesJson;

    @TableField(exist = false)
    private String description;

    @TableField(exist = false)
    private String coverImageUrl;

    @TableField(exist = false)
    private String applicableStores;

    /** 状态: 1草稿 2进行中 3已结束 4已停用 */
    private Integer status;
}
