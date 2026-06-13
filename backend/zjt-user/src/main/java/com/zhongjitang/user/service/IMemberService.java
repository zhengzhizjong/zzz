package com.zhongjitang.user.service;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.user.domain.dto.*;
import com.zhongjitang.user.domain.entity.UserMemberDO;
import com.zhongjitang.user.domain.vo.LoginResponse;
import com.zhongjitang.user.domain.vo.MemberVO;

public interface IMemberService {

    R<LoginResponse> register(RegisterRequest request);

    R<LoginResponse> login(MemberLoginRequest request);

    R<LoginResponse> wechatLogin(WechatLoginRequest request);

    R<MemberVO> getProfile(Long memberId);

    R<Void> updateProfile(Long memberId, UpdateProfileRequest request);

    R<PageResult<UserMemberDO>> page(Integer page, Integer pageSize, String keyword, Integer levelId);

    R<UserMemberDO> getDetail(Long id);

    R<Void> adminUpdate(Long id, MemberAdminUpdateRequest request);

    R<Void> bindWechat(Long memberId, String code);
}
