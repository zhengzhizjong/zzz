package com.zhongjitang.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhongjitang.common.mybatis.entity.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
@Schema(description = "字典项")
public class SysDictItemDO extends BaseDO {

    @Schema(description = "字典ID")
    private Long dictId;

    @Schema(description = "字典项编码")
    private String itemCode;

    @Schema(description = "字典项名称")
    private String itemName;

    @Schema(description = "字典项值")
    private String itemValue;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态:1启用 2停用")
    private Integer status;

    // store_id字段由BaseDO继承，sys_dict_item表有store_id列，无需额外声明
}
