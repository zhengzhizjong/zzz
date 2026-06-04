package com.zhongjitang.common.core.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TenantContext 租户上下文测试
 */
class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testSetAndGet() {
        TenantContext.TenantInfo info = new TenantContext.TenantInfo(1001L, 2001L, "standard");
        TenantContext.set(info);

        assertEquals(1001L, TenantContext.getTenantId());
        assertEquals(2001L, TenantContext.getStoreId());
        assertEquals("standard", TenantContext.get().getPlanCode());
    }

    @Test
    void testClear() {
        TenantContext.set(new TenantContext.TenantInfo(1001L, null, "basic"));
        TenantContext.clear();
        assertNull(TenantContext.getTenantId());
        assertNull(TenantContext.get());
    }

    @Test
    void testNullWhenNotSet() {
        assertNull(TenantContext.getTenantId());
        assertNull(TenantContext.getStoreId());
    }
}
