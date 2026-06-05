package com.zhongjitang.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.common.redis.util.RedisUtil;
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
import com.zhongjitang.user.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

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

    @Mock
    private RedisUtil redisUtil;

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
        mockAccount.setAccountType("phone");
        mockAccount.setAccountId("13800138000");
    }

    @Test
    void testRegister() {
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
        verify(memberMapper, times(1)).insert(any(UserMemberDO.class));
        verify(memberAccountMapper, times(1)).insert(any(UserMemberAccountDO.class));
    }

    @Test
    void testRegisterDuplicatePhone() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800138000");
        request.setVerifyCode("123456");

        when(memberAccountMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            memberService.register(request);
        });
        assertEquals(ErrorCode.DUPLICATE_PHONE.getCode(), exception.getCode());
    }

    @Test
    void testLogin() {
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
    }

    @Test
    void testLoginInvalidVerifyCode() {
        MemberLoginRequest request = new MemberLoginRequest();
        request.setPhone("13800138000");
        request.setVerifyCode("000000");

        when(smsService.verifyCode("13800138000", "000000")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            memberService.login(request);
        });
        assertEquals(ErrorCode.LOGIN_FAILED.getCode(), exception.getCode());
    }

    @Test
    void testWechatLogin() {
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("wx_code_123");
        request.setEncryptedData("data");
        request.setIv("iv");

        when(memberAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(memberMapper.insert(any(UserMemberDO.class))).thenReturn(1);
        when(memberAccountMapper.insert(any(UserMemberAccountDO.class))).thenReturn(1);
        when(jwtUtil.generateToken(any(), any(), any(), any(), any())).thenReturn("mock_token");

        R<LoginResponse> result = memberService.wechatLogin(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGetProfile() {
        UserMemberLevelDO level = new UserMemberLevelDO();
        level.setId(1L);
        level.setLevelName("普通会员");
        level.setLevelCode("NORMAL");

        mockMember.setLevelId(1L);

        when(memberMapper.selectById(1L)).thenReturn(mockMember);
        when(memberLevelMapper.selectById(1L)).thenReturn(level);

        R<MemberVO> result = memberService.getProfile(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().getLevel());
        assertEquals("普通会员", result.getData().getLevel().getLevelName());
    }

    @Test
    void testGetProfileNotFound() {
        when(memberMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            memberService.getProfile(999L);
        });
        assertEquals(ErrorCode.MEMBER_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testUpdateProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("新昵称");
        request.setRealName("张三");

        when(memberMapper.selectById(1L)).thenReturn(mockMember);
        when(memberMapper.updateById(any(UserMemberDO.class))).thenReturn(1);

        R<Void> result = memberService.updateProfile(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(memberMapper, times(1)).updateById(any(UserMemberDO.class));
    }

    @Test
    void testPage() {
        Page<UserMemberDO> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Arrays.asList(mockMember));
        pageResult.setTotal(1);

        when(memberMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        R<PageResult<UserMemberDO>> result = memberService.page(1, 20, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
    }

    @Test
    void testBindWechat() {
        when(memberMapper.selectById(1L)).thenReturn(mockMember);
        when(memberAccountMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(memberAccountMapper.insert(any(UserMemberAccountDO.class))).thenReturn(1);

        R<Void> result = memberService.bindWechat(1L, "wx_code");

        assertNotNull(result);
        assertEquals(0, result.getCode());
    }
}
