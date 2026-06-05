package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "创建部门请求")
public class DepartmentCreateRequest {

    @Schema(description = "部门名称", required = true)
    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sortOrder;
}
