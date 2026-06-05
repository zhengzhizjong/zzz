package com.zhongjitang.user.domain.vo;

import com.zhongjitang.user.domain.entity.UserDepartmentDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "部门树节点VO")
public class DepartmentTreeNodeVO {

    @Schema(description = "部门信息")
    private UserDepartmentDO department;

    @Schema(description = "子部门列表")
    private List<DepartmentTreeNodeVO> children;
}
