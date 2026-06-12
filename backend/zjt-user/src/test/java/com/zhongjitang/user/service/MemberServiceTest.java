package com.zhongjitang.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.common.security.util.JwtUtil;
import com.zhongjitang.user.domain.dto.*;
import com.zhongjitang.user.domain.entity.UserMemberAccountDO;
import com.zhongjitang.user.domain.entity.UserMemberDO;
import com.zhongjitang.user.domain.vo.LoginResponse;
import com.zhongjitang.user.domain.vo.MemberVO;
import com.zhongjitang.user.mapper.UserMemberAccountMapper;
import com.zhongjitang.user.mapper.UserMemberLevelMapper;
import com.zhongjitang.user.mapper.UserMemberMapper;
import com.zhongjitang.user.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("会员服务测试")
class MemberServiceTest {

    @Mock
    private UserMemberMapper memberMapper;

    @Mock
    private UserMemberAccountMapper memberAccountMapper;

    @Mock
    private UserMemberLevelMapper memberLevelMapper;

    @Mock
    private SmsService smsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private MemberServiceImpl memberService;

    private UserMemberDO mockMember;
    private UserMemberAccountDO mockAccount;

    @BeforeEach
    void setUp() {
        mockMember = new UserMemberDO();
        mockMember.setId(1L);
        mockMember.setMemberNo("M20260605000001");
        mockMember.setNickname("测试用户");
        mockMember.setPhone("13800138000");
        mockMember.setTotalSpent(BigDecimal.ZERO);
        mockMember.setTotalVisits(0);
        mockMember.setStatus(1);

        mockAccount = new UserMemberAccountDO();
        mockAccount.setId(1L);
        mockAccount.setMemberId(1L);
        mockAccount.setAccountType(1);
        mockAccount.setAccountId("13800138000");
    }

    @Test
    @DisplayName("会员注册-正常流程")
    void 会员注册成功() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800138000");
        request.setVerifyCode("123456");
        request.setNickname("新用户");

        when(memberAccountMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(smsService.verifyCode("13800138000", "123456")).thenReturn(true);
        when(memberMapper.insert(any(UserMemberDO.class))).thenReturn(1);
        when(memberAccountMapper.insert(any(UserMemberAccountDO.class))).thenReturn(1);
        when(jwtUtil.generateToken(any(), any(), any(), any(), any())).thenReturn("mock_token");

        R<LoginResponse> result = memberService.register(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("mock_token", result.getData().getToken());
        verify(memberMapper, times(1)).insert(any(UserMemberDO.class));
        verify(memberAccountMapper, times(1)).insert(any(UserMemberAccountDO.class));
    }

    @Test
    @DisplayName("会员注册-手机号已注册抛异常")
    void 会员注册手机号重复() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800138000");
        request.setVerifyCode("123456");

        when(memberAccountMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            memberService.register(request);
        });

        assertEquals(ErrorCode.DUPLICATE_PHONE.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("手机号已注册"));
        verify(memberMapper, never()).insert(any(UserMemberDO.class));
    }

    @Test
    @DisplayName("会员注册-验证码错误抛异常")
    void 会员注册验证码错误() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800138000");
        request.setVerifyCode("000000");

        when(memberAccountMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(smsService.verifyCode("13800138000", "000000")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            memberService.register(request);
        });

        assertEquals(ErrorCode.LOGIN_FAILED.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("验证码错误"));
        verify(memberMapper, never()).insert(any(UserMemberDO.class));
    }

    @Test
    @DisplayName("会员登录-正常流程")
    void 会员登录成功() {
        MemberLoginRequest request = new MemberLoginRequest();
        request.setPhone("13800138000");
        request.setVerifyCode("123456");

        when(smsService.verifyCode("13800138000", "123456")).thenReturn(true);
        when(memberAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockAccount);
        when(memberMapper.selectById(1L)).thenReturn(mockMember);
        when(memberMapper.updateById(any(UserMemberDO.class))).thenReturn(1);
        when(jwtUtil.generateToken(any(), any(), any(), any(), any())).thenReturn("mock_token");

        R<LoginResponse> result = memberService.login(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("mock_token", result.getData().getToken());
        assertEquals(1L, result.getData().getMemberId());
    }

    @Test
    @DisplayName("会员登录-验证码错误抛异常")
    void 会员登录验证码错误() {
        MemberLoginRequest request = new MemberLoginRequest();
        request.setPhone("13800138000");
        request.setVerifyCode("000000");

        when(smsService.verifyCode("13800138000", "000000")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            memberService.login(request);
        });

        assertEquals(ErrorCode.LOGIN_FAILED.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("验证码错误"));
    }

    @Test
    @DisplayName("会员登录-账号不存在抛异常")
    void 会员登录账号不存在() {
        MemberLoginRequest request = new MemberLoginRequest();
        request.setPhone("13800138000");
        request.setVerifyCode("123456");

        when(smsService.verifyCode("13800138000", "123456")).thenReturn(true);
        when(memberAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            memberService.login(request);
        });

        assertEquals(ErrorCode.MEMBER_NOT_FOUND.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("会员不存在"));
    }

    @Test
    @DisplayName("获取会员信息-正常流程")
    void 获取会员信息成功() {
        when(memberMapper.selectById(1L)).thenReturn(mockMember);

        R<MemberVO> result = memberService.getProfile(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getMember());
        UserMemberDO memberDO = (UserMemberDO) result.getData().getMember();
        assertEquals("测试用户", memberDO.getNickname());
    }

    @Test
    @DisplayName("获取会员信息-不存在抛异常")
    void 获取会员信息不存在() {
        when(memberMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            memberService.getProfile(999L);
        });

        assertEquals(ErrorCode.MEMBER_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("微信登录-新用户自动注册")
    void 微信登录新用户自动注册() {
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("wx_code_123");

        when(memberAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(memberMapper.insert(any(UserMemberDO.class))).thenReturn(1);
        when(memberAccountMapper.insert(any(UserMemberAccountDO.class))).thenReturn(1);
        when(jwtUtil.generateToken(any(), any(), any(), any(), any())).thenReturn("mock_token");

        R<LoginResponse> result = memberService.wechatLogin(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        verify(memberMapper, times(1)).insert(any(UserMemberDO.class));
        verify(memberAccountMapper, times(1)).insert(any(UserMemberAccountDO.class));
    }
}
