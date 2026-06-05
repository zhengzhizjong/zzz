package com.zhongjitang.user.service.impl;

import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.redis.util.RedisUtil;
import com.zhongjitang.user.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final RedisUtil redisUtil;

    private static final String VERIFY_CODE_PREFIX = "sms:verify:";
    private static final String RATE_LIMIT_PREFIX = "sms:rate:";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int RATE_LIMIT_SECONDS = 60;

    @Override
    public void sendVerifyCode(String phone) {
        // 60秒限流
        String rateLimitKey = RATE_LIMIT_PREFIX + phone;
        if (Boolean.TRUE.equals(redisUtil.hasKey(rateLimitKey))) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, "验证码发送过于频繁，请稍后重试");
        }

        // 生成6位数字验证码
        String code = generateCode();

        // 存储验证码到Redis，5分钟有效
        String codeKey = VERIFY_CODE_PREFIX + phone;
        redisUtil.setWithExpire(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 设置限流标记
        redisUtil.setWithExpire(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        // TODO: 调用短信服务发送验证码
        log.info("发送验证码: phone={}, code={}", phone, code);
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        String codeKey = VERIFY_CODE_PREFIX + phone;
        Object storedCode = redisUtil.get(codeKey);
        if (storedCode == null) {
            return false;
        }
        if (storedCode.toString().equals(code)) {
            // 验证成功后删除验证码
            redisUtil.delete(codeKey);
            return true;
        }
        return false;
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
