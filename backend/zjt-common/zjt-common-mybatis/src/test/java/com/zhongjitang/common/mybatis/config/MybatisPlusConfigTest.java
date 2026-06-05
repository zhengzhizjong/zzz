package com.zhongjitang.common.mybatis.config;

import com.zhongjitang.common.mybatis.handler.TenantLineHandlerImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MyBatis Plus配置测试
 */
class MybatisPlusConfigTest {

    @Test
    @DisplayName("TenantLineHandlerImpl - 忽略表应返回true")
    void tenantLineHandler_ignoreTable() {
        TenantLineHandlerImpl handler = new TenantLineHandlerImpl();

        assertTrue(handler.ignoreTable("billing_tenant"));
        assertTrue(handler.ignoreTable("billing_plan"));
        assertTrue(handler.ignoreTable("sys_dict"));
        assertTrue(handler.ignoreTable("sys_dict_item"));
        assertTrue(handler.ignoreTable("sys_config"));
        assertTrue(handler.ignoreTable("sys_feature_flag"));
    }

    @Test
    @DisplayName("TenantLineHandlerImpl - 业务表不应忽略")
    void tenantLineHandler_notIgnoreTable() {
        TenantLineHandlerImpl handler = new TenantLineHandlerImpl();

        assertFalse(handler.ignoreTable("member"));
        assertFalse(handler.ignoreTable("appointment"));
        assertFalse(handler.ignoreTable("store"));
    }

    @Test
    @DisplayName("TenantLineHandlerImpl - 租户字段名应为tenant_id")
    void tenantLineHandler_tenantIdColumn() {
        TenantLineHandlerImpl handler = new TenantLineHandlerImpl();
        assertEquals("tenant_id", handler.getTenantIdColumn());
    }

    @Test
    @DisplayName("MybatisPlusConfig - 应正常创建拦截器Bean")
    void mybatisPlusInterceptor() {
        MybatisPlusConfig config = new MybatisPlusConfig();
        assertNotNull(config.mybatisPlusInterceptor());
    }

    @Test
    @DisplayName("MybatisPlusConfig - 应正常创建MetaObjectHandler Bean")
    void metaObjectHandler() {
        MybatisPlusConfig config = new MybatisPlusConfig();
        assertNotNull(config.metaObjectHandler());
    }
}
