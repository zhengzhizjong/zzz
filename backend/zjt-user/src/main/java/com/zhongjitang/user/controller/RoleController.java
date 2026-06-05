package com.zhongjitang.user.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.AssignPermissionRequest;
import com.zhongjitang.user.domain.dto.RoleCreateRequest;
import com.zhongjitang.user.domain.dto.RoleUpdateRequest;
import com.zhongjitang.user.domain.entity.UserRoleDO;
import com.zhongjitang.user.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user/roles")
@Tag(name = "角色管理")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;

    @GetMapping
    @Operation(summary = "角色列表")
    public R<List<UserRoleDO>> list() {
        return roleService.list();
    }

    @PostMapping
    @Operation(summary = "创建角色")
    public R<Void> create(@Valid @RequestBody RoleCreateRequest request) {
        return roleService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return roleService.update(id, request);
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "分配权限")
    public R<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody AssignPermissionRequest request) {
        return roleService.assignPermissions(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public R<Void> delete(@PathVariable Long id) {
        return roleService.delete(id);
    }
}
