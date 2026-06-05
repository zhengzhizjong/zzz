package com.zhongjitang.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_feature_flag")
@Schema(description = "功能开关")
public class SysFeatureFlagDO extends BaseDO {

    @Schema(description = "开关键")
    private String flagKey;

    @Schema(description = "开关名称")
    private String flagName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "默认值:0关 1开")
    private Integer defaultValue;

    @Schema(description = "类型:1全局 2租户 3门店 4用户 5百分比")
    private Integer type;

    @Schema(description = "百分比(0-100)")
    private Integer percentage;

    @Schema(description = "规则(JSON)")
    private String rulesJson;

    // store_id字段由BaseDO继承，sys_feature_flag表有store_id列，无需额外声明
}
