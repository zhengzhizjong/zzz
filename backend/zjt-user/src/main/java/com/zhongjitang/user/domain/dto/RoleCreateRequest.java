package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "创建角色请求")
public class RoleCreateRequest {

    @Schema(description = "角色名称", required = true)
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @Schema(description = "角色编码", required = true)
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @Schema(description = "描述")
    private String description;
}
