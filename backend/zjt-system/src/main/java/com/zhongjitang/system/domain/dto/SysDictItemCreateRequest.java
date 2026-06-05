package com.zhongjitang.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "创建字典项请求")
public class SysDictItemCreateRequest {

    @NotNull(message = "字典ID不能为空")
    @Schema(description = "字典ID")
    private Long dictId;

    @NotBlank(message = "字典项编码不能为空")
    @Schema(description = "字典项编码", example = "male")
    private String itemCode;

    @NotBlank(message = "字典项名称不能为空")
    @Schema(description = "字典项名称", example = "男")
    private String itemName;

    @Schema(description = "字典项值")
    private String itemValue;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态:1启用 2停用")
    private Integer status;
}
