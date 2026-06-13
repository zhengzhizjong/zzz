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
import java.time.LocalDateTime;
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
        LambdaQueryWrapper<UserMemberDO> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(UserMemberDO::getPhone, request.getPhone());
        Long count = memberMapper.selectCount(memberWrapper);
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
        member.setName(StringUtils.hasText(request.getNickname()) ? request.getNickname() : "用户" + request.getPhone().substring(7));
        member.setPhone(request.getPhone());
        member.setMemberType(1); // 普通会员
        member.setPoints(0);
        member.setBalance(BigDecimal.ZERO);
        member.setSource("phone");
        member.setStatus(1);
        memberMapper.insert(member);

        // 创建余额账户
        UserMemberAccountDO account = new UserMemberAccountDO();
        account.setMemberId(member.getId());
        account.setAccountType(2); // 2=余额
        account.setBalance(BigDecimal.ZERO);
        account.setFrozenAmount(BigDecimal.ZERO);
        memberAccountMapper.insert(account);

        return R.ok(buildLoginResponse(member));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<LoginResponse> login(MemberLoginRequest request) {
        // 验证验证码
        if (!smsService.verifyCode(request.getPhone(), request.getVerifyCode())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "验证码错误");
        }

        // 通过手机号查找会员，不存在则自动注册
        LambdaQueryWrapper<UserMemberDO> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(UserMemberDO::getPhone, request.getPhone());
        UserMemberDO member = memberMapper.selectOne(memberWrapper);
        if (member == null) {
            // 自动注册
            member = new UserMemberDO();
            member.setMemberNo(generateMemberNo());
            member.setName("用户" + request.getPhone().substring(7));
            member.setPhone(request.getPhone());
            member.setMemberType(1);
            member.setPoints(0);
            member.setBalance(BigDecimal.ZERO);
            member.setSource("phone");
            member.setStatus(1);
            memberMapper.insert(member);

            UserMemberAccountDO account = new UserMemberAccountDO();
            account.setMemberId(member.getId());
            account.setAccountType(2);
            account.setBalance(BigDecimal.ZERO);
            account.setFrozenAmount(BigDecimal.ZERO);
            memberAccountMapper.insert(account);

            log.info("会员自动注册: phone={}, memberNo={}", request.getPhone(), member.getMemberNo());
        } else {
            // 更新最后访问时间
            member.setLastVisitAt(LocalDateTime.now());
            member.setTotalVisits(member.getTotalVisits() != null ? member.getTotalVisits() + 1 : 1);
            memberMapper.updateById(member);
        }

        return R.ok(buildLoginResponse(member));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<LoginResponse> wechatLogin(WechatLoginRequest request) {
        String openId = "wx_" + request.getCode();

        // 简化：创建新会员
        UserMemberDO member = new UserMemberDO();
        member.setMemberNo(generateMemberNo());
        member.setName("微信用户");
        member.setMemberType(1);
        member.setPoints(0);
        member.setBalance(BigDecimal.ZERO);
        member.setSource("wechat");
        member.setStatus(1);
        memberMapper.insert(member);

        UserMemberAccountDO account = new UserMemberAccountDO();
        account.setMemberId(member.getId());
        account.setAccountType(2); // 2=余额
        account.setBalance(BigDecimal.ZERO);
        account.setFrozenAmount(BigDecimal.ZERO);
        memberAccountMapper.insert(account);

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
            member.setName(request.getNickname());
        }
        if (request.getGender() != null) {
            member.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            member.setBirthday(request.getBirthday());
        }

        memberMapper.updateById(member);
        return R.ok();
    }

    @Override
    public R<PageResult<UserMemberDO>> page(Integer page, Integer pageSize, String keyword, Integer levelId) {
        Page<UserMemberDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserMemberDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(UserMemberDO::getName, keyword)
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
    public R<UserMemberDO> getDetail(Long id) {
        UserMemberDO member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return R.ok(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> adminUpdate(Long id, MemberAdminUpdateRequest request) {
        UserMemberDO member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (request.getName() != null) {
            member.setName(request.getName());
        }
        if (request.getRealName() != null) {
            member.setRealName(request.getRealName());
        }
        if (request.getPhone() != null) {
            member.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            member.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            member.setBirthday(request.getBirthday());
        }
        if (request.getMemberType() != null) {
            member.setMemberType(request.getMemberType());
        }
        if (request.getStatus() != null) {
            member.setStatus(request.getStatus());
        }

        memberMapper.updateById(member);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> bindWechat(Long memberId, String code) {
        UserMemberDO member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        LambdaQueryWrapper<UserMemberAccountDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMemberAccountDO::getMemberId, memberId)
                .eq(UserMemberAccountDO::getAccountType, 3); // 3=疗程(微信绑定)
        Long count = memberAccountMapper.selectCount(wrapper);
        if (count > 0) {
            return R.fail("已绑定微信账号");
        }

        UserMemberAccountDO account = new UserMemberAccountDO();
        account.setMemberId(memberId);
        account.setAccountType(3); // 3=疗程(微信绑定)
        account.setBalance(BigDecimal.ZERO);
        account.setFrozenAmount(BigDecimal.ZERO);
        memberAccountMapper.insert(account);

        return R.ok();
    }

    private LoginResponse buildLoginResponse(UserMemberDO member) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = 1L; // 默认租户
        }
        Long storeId = TenantContext.getStoreId();
        if (storeId == null) {
            storeId = 0L; // 默认门店
        }
        String token = jwtUtil.generateToken(member.getId(), member.getName(), "member", tenantId, storeId);
        String refreshToken = jwtUtil.generateToken(member.getId(), member.getName(), "member", tenantId, storeId);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setMemberId(member.getId());
        response.setNickname(member.getName());
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
