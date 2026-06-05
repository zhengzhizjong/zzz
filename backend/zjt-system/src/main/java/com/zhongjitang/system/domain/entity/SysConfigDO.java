package com.zhongjitang.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
@Schema(description = "系统配置")
public class SysConfigDO extends BaseDO {

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "配置类型:1字符串 2数字 3布尔 4JSON")
    private Integer configType;

    // store_id字段由BaseDO继承，sys_config表有store_id列，无需额外声明
}
