package com.zhongjitang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分配权限请求")
public class AssignPermissionRequest {

    @Schema(description = "权限ID列表", required = true)
    private List<Long> permissionIds;
}
