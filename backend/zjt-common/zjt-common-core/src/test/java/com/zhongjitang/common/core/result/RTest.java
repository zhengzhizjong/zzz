package com.zhongjitang.common.core.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("R统一响应体测试")
class RTest {

    @Test
    @DisplayName("ok无参应返回code=0且data为null")
    void ok无参返回成功响应() {
        R<Void> result = R.ok();

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("ok带数据应返回code=0且data不为null")
    void ok带数据返回成功响应() {
        String data = "测试数据";

        R<String> result = R.ok(data);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("测试数据", result.getData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("ok带数据和自定义消息应正确返回")
    void ok带数据和自定义消息() {
        String data = "hello";

        R<String> result = R.ok(data, "操作成功");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("hello", result.getData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("fail带code和message应返回失败响应")
    void fail带错误码和消息() {
        R<Void> result = R.fail(40001, "参数错误");

        assertNotNull(result);
        assertEquals(40001, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("fail仅带message应使用默认错误码50001")
    void fail仅带消息使用默认错误码() {
        R<Void> result = R.fail("系统异常");

        assertNotNull(result);
        assertEquals(50001, result.getCode());
        assertEquals("系统异常", result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("requestId链式调用应正确设置")
    void requestId链式调用() {
        R<String> result = R.ok("data").requestId("req-001");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("req-001", result.getRequestId());
        assertEquals("data", result.getData());
    }

    @Test
    @DisplayName("ok和fail的code应不同")
    void ok和fail的code应不同() {
        R<String> okResult = R.ok("data");
        R<Void> failResult = R.fail("错误");

        assertNotEquals(okResult.getCode(), failResult.getCode());
        assertTrue(okResult.getCode() < failResult.getCode());
        assertEquals(0, okResult.getCode());
        assertEquals(50001, failResult.getCode());
    }
}
