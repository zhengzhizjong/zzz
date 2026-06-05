package com.zhongjitang.user.domain.vo;

import com.zhongjitang.user.domain.entity.UserPermissionDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "权限树节点VO")
public class PermissionTreeNodeVO {

    @Schema(description = "权限信息")
    private UserPermissionDO permission;

    @Schema(description = "子权限列表")
    private List<PermissionTreeNodeVO> children;
}
