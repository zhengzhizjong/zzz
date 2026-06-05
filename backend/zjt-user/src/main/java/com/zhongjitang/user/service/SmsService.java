package com.zhongjitang.user.service;

public interface SmsService {

    void sendVerifyCode(String phone);

    boolean verifyCode(String phone, String code);
}
