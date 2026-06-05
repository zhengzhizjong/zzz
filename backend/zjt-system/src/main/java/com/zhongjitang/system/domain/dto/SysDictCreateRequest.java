package com.zhongjitang.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "创建字典请求")
public class SysDictCreateRequest {

    @NotBlank(message = "字典编码不能为空")
    @Schema(description = "字典编码", example = "gender")
    private String dictCode;

    @NotBlank(message = "字典名称不能为空")
    @Schema(description = "字典名称", example = "性别")
    private String dictName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态:1启用 2停用")
    private Integer status;
}
