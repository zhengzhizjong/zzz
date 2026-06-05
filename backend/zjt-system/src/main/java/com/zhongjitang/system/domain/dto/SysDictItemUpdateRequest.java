package com.zhongjitang.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新字典项请求")
public class SysDictItemUpdateRequest {

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
}
