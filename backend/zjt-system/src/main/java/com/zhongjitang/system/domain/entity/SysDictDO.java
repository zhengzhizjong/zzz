package com.zhongjitang.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
@Schema(description = "字典")
public class SysDictDO extends BaseDO {

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态:1启用 2停用")
    private Integer status;

    // store_id字段由BaseDO继承，sys_dict表有store_id列，无需额外声明
}
