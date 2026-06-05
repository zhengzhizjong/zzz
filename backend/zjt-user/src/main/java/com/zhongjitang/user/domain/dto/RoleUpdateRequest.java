package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新角色请求")
public class RoleUpdateRequest {

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;
}
