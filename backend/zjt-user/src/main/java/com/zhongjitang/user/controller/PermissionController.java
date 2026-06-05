package com.zhongjitang.user.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.PermissionCreateRequest;
import com.zhongjitang.user.domain.dto.PermissionUpdateRequest;
import com.zhongjitang.user.domain.vo.PermissionTreeNodeVO;
import com.zhongjitang.user.service.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user/permissions")
@Tag(name = "权限管理")
@RequiredArgsConstructor
public class PermissionController {

    private final IPermissionService permissionService;

    @GetMapping("/tree")
    @Operation(summary = "权限树")
    public R<List<PermissionTreeNodeVO>> tree() {
        return permissionService.tree();
    }

    @PostMapping
    @Operation(summary = "创建权限")
    public R<Void> create(@Valid @RequestBody PermissionCreateRequest request) {
        return permissionService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新权限")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody PermissionUpdateRequest request) {
        return permissionService.update(id, request);
    }
}
