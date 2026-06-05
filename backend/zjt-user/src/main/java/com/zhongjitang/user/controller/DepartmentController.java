package com.zhongjitang.user.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.DepartmentCreateRequest;
import com.zhongjitang.user.domain.dto.DepartmentUpdateRequest;
import com.zhongjitang.user.domain.vo.DepartmentTreeNodeVO;
import com.zhongjitang.user.service.IDepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user/departments")
@Tag(name = "部门管理")
@RequiredArgsConstructor
public class DepartmentController {

    private final IDepartmentService departmentService;

    @GetMapping
    @Operation(summary = "部门树")
    public R<List<DepartmentTreeNodeVO>> tree() {
        return departmentService.tree();
    }

    @PostMapping
    @Operation(summary = "创建部门")
    public R<Void> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return departmentService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody DepartmentUpdateRequest request) {
        return departmentService.update(id, request);
    }
}
