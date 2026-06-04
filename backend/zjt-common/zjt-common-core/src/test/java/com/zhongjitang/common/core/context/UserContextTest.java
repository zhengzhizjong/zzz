package com.zhongjitang.common.core.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserContext 用户上下文测试
 */
class UserContextTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void testSetAndGet() {
        UserContext.UserInfo info = new UserContext.UserInfo(5001L, "张三", "member", 2001L);
        UserContext.set(info);

        assertEquals(5001L, UserContext.getUserId());
        assertEquals("张三", UserContext.getUsername());
        assertEquals("member", UserContext.getUserType());
        assertEquals(2001L, UserContext.get().getStoreId());
    }

    @Test
    void testClear() {
        UserContext.set(new UserContext.UserInfo(1L, "admin", "admin", null));
        UserContext.clear();
        assertNull(UserContext.getUserId());
        assertNull(UserContext.get());
    }

    @Test
    void testNullWhenNotSet() {
        assertNull(UserContext.getUserId());
        assertNull(UserContext.getUsername());
        assertNull(UserContext.getUserType());
    }
}
