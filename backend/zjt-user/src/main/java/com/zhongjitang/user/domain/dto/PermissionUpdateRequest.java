package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新权限请求")
public class PermissionUpdateRequest {

    @Schema(description = "权限名称")
    private String permissionName;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "资源类型: menu/button/api")
    private String resourceType;

    @Schema(description = "资源ID")
    private String resourceId;

    @Schema(description = "父权限ID")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;
}
