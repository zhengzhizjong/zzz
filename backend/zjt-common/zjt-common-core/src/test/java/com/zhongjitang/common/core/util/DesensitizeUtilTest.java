package com.zhongjitang.common.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 脱敏工具类测试
 */
class DesensitizeUtilTest {

    @Test
    @DisplayName("手机号脱敏 - 13800138000 → 138****8000")
    void desensitizePhone() {
        assertEquals("138****8000", DesensitizeUtil.desensitizePhone("13800138000"));
    }

    @Test
    @DisplayName("手机号脱敏 - 短号码应原样返回")
    void desensitizePhone_short() {
        assertEquals("138", DesensitizeUtil.desensitizePhone("138"));
    }

    @Test
    @DisplayName("手机号脱敏 - null应返回null")
    void desensitizePhone_null() {
        assertNull(DesensitizeUtil.desensitizePhone(null));
    }

    @Test
    @DisplayName("身份证号脱敏 - 110101199001011234 → 110101****1234")
    void desensitizeIdCard() {
        assertEquals("110101****1234", DesensitizeUtil.desensitizeIdCard("110101199001011234"));
    }

    @Test
    @DisplayName("身份证号脱敏 - 短号码应原样返回")
    void desensitizeIdCard_short() {
        assertEquals("123456789", DesensitizeUtil.desensitizeIdCard("123456789"));
    }

    @Test
    @DisplayName("身份证号脱敏 - null应返回null")
    void desensitizeIdCard_null() {
        assertNull(DesensitizeUtil.desensitizeIdCard(null));
    }

    @Test
    @DisplayName("姓名脱敏 - 张三丰 → 张**")
    void desensitizeName() {
        assertEquals("张**", DesensitizeUtil.desensitizeName("张三丰"));
    }

    @Test
    @DisplayName("姓名脱敏 - 两个字的姓名 → 张**")
    void desensitizeName_twoChars() {
        assertEquals("张**", DesensitizeUtil.desensitizeName("张三"));
    }

    @Test
    @DisplayName("姓名脱敏 - 单字姓名应原样返回")
    void desensitizeName_singleChar() {
        assertEquals("张", DesensitizeUtil.desensitizeName("张"));
    }

    @Test
    @DisplayName("姓名脱敏 - null应返回null")
    void desensitizeName_null() {
        assertNull(DesensitizeUtil.desensitizeName(null));
    }
}
