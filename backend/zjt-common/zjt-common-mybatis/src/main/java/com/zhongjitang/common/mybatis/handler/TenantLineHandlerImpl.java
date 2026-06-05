package com.zhongjitang.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.zhongjitang.common.core.context.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 多租户处理器实现
 * <p>
 * 从TenantContext中获取当前租户ID，自动在SQL中追加租户条件。
 * 忽略不需要租户隔离的系统表。
 * </p>
 */
public class TenantLineHandlerImpl implements TenantLineHandler {

    /** 不需要租户隔离的表 */
    private static final Set<String> IGNORE_TABLES = new HashSet<>(Arrays.asList(
            "billing_tenant",
            "billing_plan",
            "sys_dict",
            "sys_dict_item",
            "sys_config",
            "sys_feature_flag"
    ));

    /**
     * 获取当前租户ID
     * <p>
     * 从TenantContext中获取，如果上下文中没有租户信息则返回NullValue，
     * 这样不会在SQL中追加租户条件。
     * </p>
     *
     * @return 租户ID表达式
     */
    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return new NullValue();
        }
        return new LongValue(tenantId);
    }

    /**
     * 获取租户字段名
     *
     * @return 租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    /**
     * 判断表是否忽略租户隔离
     *
     * @param tableName 表名
     * @return true=忽略, false=不忽略
     */
    @Override
    public boolean ignoreTable(String tableName) {
        return IGNORE_TABLES.contains(tableName);
    }
}
