package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新部门请求")
public class DepartmentUpdateRequest {

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;
}
