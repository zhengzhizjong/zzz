package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "创建权限请求")
public class PermissionCreateRequest {

    @Schema(description = "权限名称", required = true)
    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    @Schema(description = "权限编码", required = true)
    @NotBlank(message = "权限编码不能为空")
    private String permissionCode;

    @Schema(description = "资源类型: menu/button/api")
    private String resourceType;

    @Schema(description = "资源ID")
    private String resourceId;

    @Schema(description = "父权限ID")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sortOrder;
}
