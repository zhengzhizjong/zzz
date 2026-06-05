package com.zhongjitang.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新字典请求")
public class SysDictUpdateRequest {

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态:1启用 2停用")
    private Integer status;
}
