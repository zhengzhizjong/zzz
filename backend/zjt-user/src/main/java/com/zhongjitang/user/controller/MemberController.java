package com.zhongjitang.user.controller;

import com.zhongjitang.common.core.context.UserContext;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.*;
import com.zhongjitang.user.domain.entity.UserMemberDO;
import com.zhongjitang.user.domain.vo.LoginResponse;
import com.zhongjitang.user.domain.vo.MemberVO;
import com.zhongjitang.user.service.IMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user/members")
@Tag(name = "会员管理")
@RequiredArgsConstructor
public class MemberController {

    private final IMemberService memberService;

    @PostMapping("/register")
    @Operation(summary = "会员注册")
    public R<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return memberService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "会员登录")
    public R<LoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        return memberService.login(request);
    }

    @PostMapping("/wechat-login")
    @Operation(summary = "微信登录")
    public R<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return memberService.wechatLogin(request);
    }

    @GetMapping("/profile")
    @Operation(summary = "个人信息")
    public R<MemberVO> getProfile() {
        Long memberId = UserContext.getUserId();
        return memberService.getProfile(memberId);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人信息")
    public R<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long memberId = UserContext.getUserId();
        return memberService.updateProfile(memberId, request);
    }

    @GetMapping
    @Operation(summary = "会员列表(管理端)")
    public R<PageResult<UserMemberDO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer levelId,
            @RequestParam(required = false) Long storeId) {
        return memberService.page(page, pageSize, keyword, levelId, storeId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "会员详情(管理端)")
    public R<UserMemberDO> getDetail(@PathVariable Long id) {
        return memberService.getDetail(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新会员(管理端)")
    public R<Void> updateMember(@PathVariable Long id, @RequestBody MemberAdminUpdateRequest request) {
        return memberService.adminUpdate(id, request);
    }

    @PostMapping("/bind-wechat")
    @Operation(summary = "绑定微信")
    public R<Void> bindWechat(@RequestParam String code) {
        Long memberId = UserContext.getUserId();
        return memberService.bindWechat(memberId, code);
    }
}
