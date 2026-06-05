package com.zhongjitang.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.context.TenantContext;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.common.security.util.JwtUtil;
import com.zhongjitang.user.domain.dto.*;
import com.zhongjitang.user.domain.entity.UserMemberAccountDO;
import com.zhongjitang.user.domain.entity.UserMemberDO;
import com.zhongjitang.user.domain.entity.UserMemberLevelDO;
import com.zhongjitang.user.domain.vo.LoginResponse;
import com.zhongjitang.user.domain.vo.MemberVO;
import com.zhongjitang.user.mapper.UserMemberAccountMapper;
import com.zhongjitang.user.mapper.UserMemberLevelMapper;
import com.zhongjitang.user.mapper.UserMemberMapper;
import com.zhongjitang.user.service.IMemberService;
import com.zhongjitang.user.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements IMemberService {

    private final UserMemberMapper memberMapper;
    private final UserMemberAccountMapper memberAccountMapper;
    private final UserMemberLevelMapper memberLevelMapper;
    private final SmsService smsService;
    private final JwtUtil jwtUtil;

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);
    private static final String MEMBER_NO_PREFIX = "M";
    private static final String MEMBER_NO_DATE_FORMAT = "yyyyMMdd";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<LoginResponse> register(RegisterRequest request) {
        // 检查手机号是否已注册
        LambdaQueryWrapper<UserMemberAccountDO> accountWrapper = new LambdaQueryWrapper<>();
        accountWrapper.eq(UserMemberAccountDO::getAccountType, "phone")
                .eq(UserMemberAccountDO::getAccountId, request.getPhone());
        Long count = memberAccountMapper.selectCount(accountWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }

        // 验证验证码
        if (!smsService.verifyCode(request.getPhone(), request.getVerifyCode())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "验证码错误");
        }

        // 创建会员
        UserMemberDO member = new UserMemberDO();
        member.setMemberNo(generateMemberNo());
        member.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : "用户" + request.getPhone().substring(7));
        member.setPhone(request.getPhone());
        member.setTotalSpent(BigDecimal.ZERO);
        member.setTotalVisits(0);
        member.setSource("phone");
        member.setStatus(1);
        memberMapper.insert(member);

        // 创建账号
        UserMemberAccountDO account = new UserMemberAccountDO();
        account.setMemberId(member.getId());
        account.setAccountType("phone");
        account.setAccountId(request.getPhone());
        memberAccountMapper.insert(account);

        // 生成Token
        return R.ok(buildLoginResponse(member));
    }

    @Override
    public R<LoginResponse> login(MemberLoginRequest request) {
        // 验证验证码
        if (!smsService.verifyCode(request.getPhone(), request.getVerifyCode())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "验证码错误");
        }

        // 查找账号
        LambdaQueryWrapper<UserMemberAccountDO> accountWrapper = new LambdaQueryWrapper<>();
        accountWrapper.eq(UserMemberAccountDO::getAccountType, "phone")
                .eq(UserMemberAccountDO::getAccountId, request.getPhone());
        UserMemberAccountDO account = memberAccountMapper.selectOne(accountWrapper);
        if (account == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "会员不存在，请先注册");
        }

        // 查找会员
        UserMemberDO member = memberMapper.selectById(account.getMemberId());
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 更新最后访问时间
        member.setLastVisitAt(java.time.LocalDateTime.now());
        member.setTotalVisits(member.getTotalVisits() + 1);
        memberMapper.updateById(member);

        return R.ok(buildLoginResponse(member));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<LoginResponse> wechatLogin(WechatLoginRequest request) {
        // TODO: 调用微信API获取openId和unionId，此处模拟
        String openId = "wx_" + request.getCode();
        String unionId = "wx_union_" + request.getCode();

        // 查找微信账号
        LambdaQueryWrapper<UserMemberAccountDO> accountWrapper = new LambdaQueryWrapper<>();
        accountWrapper.eq(UserMemberAccountDO::getAccountType, "wechat")
                .eq(UserMemberAccountDO::getOpenId, openId);
        UserMemberAccountDO account = memberAccountMapper.selectOne(accountWrapper);

        UserMemberDO member;
        if (account != null) {
            // 已有微信账号，直接登录
            member = memberMapper.selectById(account.getMemberId());
            if (member == null) {
                throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
            }
            member.setLastVisitAt(java.time.LocalDateTime.now());
            member.setTotalVisits(member.getTotalVisits() + 1);
            memberMapper.updateById(member);
        } else {
            // 自动注册
            member = new UserMemberDO();
            member.setMemberNo(generateMemberNo());
            member.setNickname("微信用户");
            member.setTotalSpent(BigDecimal.ZERO);
            member.setTotalVisits(1);
            member.setSource("wechat");
            member.setStatus(1);
            memberMapper.insert(member);

            account = new UserMemberAccountDO();
            account.setMemberId(member.getId());
            account.setAccountType("wechat");
            account.setAccountId(openId);
            account.setOpenId(openId);
            account.setUnionId(unionId);
            memberAccountMapper.insert(account);
        }

        return R.ok(buildLoginResponse(member));
    }

    @Override
    public R<MemberVO> getProfile(Long memberId) {
        UserMemberDO member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        MemberVO vo = new MemberVO();
        vo.setMember(member);
        vo.setTotalSpent(member.getTotalSpent());
        vo.setTotalVisits(member.getTotalVisits());

        // 查询等级信息
        if (member.getLevelId() != null) {
            UserMemberLevelDO level = memberLevelMapper.selectById(member.getLevelId());
            vo.setLevel(level);
        }

        return R.ok(vo);
    }

    @Override
    public R<Void> updateProfile(Long memberId, UpdateProfileRequest request) {
        UserMemberDO member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (request.getNickname() != null) {
            member.setNickname(request.getNickname());
        }
        if (request.getRealName() != null) {
            member.setRealName(request.getRealName());
        }
        if (request.getGender() != null) {
            member.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            member.setBirthday(request.getBirthday());
        }
        if (request.getAvatarUrl() != null) {
            member.setAvatarUrl(request.getAvatarUrl());
        }

        memberMapper.updateById(member);
        return R.ok();
    }

    @Override
    public R<PageResult<UserMemberDO>> page(Integer page, Integer pageSize, String keyword, Integer levelId) {
        Page<UserMemberDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserMemberDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(UserMemberDO::getNickname, keyword)
                    .or().like(UserMemberDO::getRealName, keyword)
                    .or().like(UserMemberDO::getPhone, keyword)
                    .or().like(UserMemberDO::getMemberNo, keyword));
        }
        if (levelId != null) {
            wrapper.eq(UserMemberDO::getLevelId, levelId);
        }
        wrapper.orderByDesc(UserMemberDO::getCreatedAt);
        Page<UserMemberDO> result = memberMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> bindWechat(Long memberId, String code) {
        UserMemberDO member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // TODO: 调用微信API获取openId
        String openId = "wx_" + code;

        // 检查是否已绑定
        LambdaQueryWrapper<UserMemberAccountDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMemberAccountDO::getMemberId, memberId)
                .eq(UserMemberAccountDO::getAccountType, "wechat");
        Long count = memberAccountMapper.selectCount(wrapper);
        if (count > 0) {
            return R.fail("已绑定微信账号");
        }

        UserMemberAccountDO account = new UserMemberAccountDO();
        account.setMemberId(memberId);
        account.setAccountType("wechat");
        account.setAccountId(openId);
        account.setOpenId(openId);
        memberAccountMapper.insert(account);

        return R.ok();
    }

    private LoginResponse buildLoginResponse(UserMemberDO member) {
        Long tenantId = TenantContext.getTenantId();
        Long storeId = TenantContext.getStoreId();
        String token = jwtUtil.generateToken(member.getId(), member.getNickname(), "member", tenantId, storeId);
        String refreshToken = jwtUtil.generateToken(member.getId(), member.getNickname(), "member", tenantId, storeId);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setMemberId(member.getId());
        response.setNickname(member.getNickname());
        response.setAvatarUrl(member.getAvatarUrl());
        response.setPhone(member.getPhone());
        return response;
    }

    private String generateMemberNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern(MEMBER_NO_DATE_FORMAT));
        int seq = SEQUENCE.getAndIncrement();
        if (seq > 999999) {
            SEQUENCE.set(1);
            seq = 1;
        }
        return MEMBER_NO_PREFIX + datePart + String.format("%06d", seq);
    }
}
