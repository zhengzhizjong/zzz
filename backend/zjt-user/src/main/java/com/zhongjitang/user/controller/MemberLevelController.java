package com.zhongjitang.user.controller;

import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.MemberLevelCreateRequest;
import com.zhongjitang.user.domain.dto.MemberLevelUpdateRequest;
import com.zhongjitang.user.domain.entity.UserMemberLevelDO;
import com.zhongjitang.user.service.IMemberLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user/member-levels")
@Tag(name = "会员等级管理")
@RequiredArgsConstructor
public class MemberLevelController {

    private final IMemberLevelService memberLevelService;

    @GetMapping
    @Operation(summary = "等级列表")
    public R<List<UserMemberLevelDO>> list() {
        return memberLevelService.list();
    }

    @PostMapping
    @Operation(summary = "创建等级")
    public R<Void> create(@Valid @RequestBody MemberLevelCreateRequest request) {
        return memberLevelService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新等级")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody MemberLevelUpdateRequest request) {
        return memberLevelService.update(id, request);
    }
}
