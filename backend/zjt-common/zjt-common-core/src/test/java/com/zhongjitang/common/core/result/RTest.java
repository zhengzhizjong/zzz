package com.zhongjitang.common.core.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * R 统一响应体测试
 */
class RTest {

    @Test
    void testOk() {
        R<String> r = R.ok("hello");
        assertEquals(0, r.getCode());
        assertEquals("success", r.getMessage());
        assertEquals("hello", r.getData());
        assertNotNull(r.getTimestamp());
    }

    @Test
    void testOkWithoutData() {
        R<Void> r = R.ok();
        assertEquals(0, r.getCode());
        assertEquals("success", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void testOkWithMessage() {
        R<String> r = R.ok("data", "操作成功");
        assertEquals(0, r.getCode());
        assertEquals("操作成功", r.getMessage());
        assertEquals("data", r.getData());
    }

    @Test
    void testFail() {
        R<Void> r = R.fail(40001, "参数错误");
        assertEquals(40001, r.getCode());
        assertEquals("参数错误", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void testFailDefault() {
        R<Void> r = R.fail("系统异常");
        assertEquals(50001, r.getCode());
        assertEquals("系统异常", r.getMessage());
    }

    @Test
    void testRequestId() {
        R<String> r = R.ok("data").requestId("req-123");
        assertEquals("req-123", r.getRequestId());
    }
}
