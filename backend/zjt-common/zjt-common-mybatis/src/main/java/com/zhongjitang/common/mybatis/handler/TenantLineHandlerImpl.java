package com.zhongjitang.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.zhongjitang.common.core.context.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 多租户处理器实现
 * <p>
 * 从TenantContext中获取当前租户ID，自动在SQL中追加租户条件。
 * 当没有租户上下文时，使用默认租户ID=1，避免NULL值约束冲突。
 * 忽略不需要租户隔离的系统表。
 * </p>
 */
public class TenantLineHandlerImpl implements TenantLineHandler {

    /** 默认租户ID */
    private static final Long DEFAULT_TENANT_ID = 1L;

    /** 不需要租户隔离的表 */
    private static final Set<String> IGNORE_TABLES = new HashSet<>(Arrays.asList(
            "billing_tenant",
            "billing_plan",
            "sys_dict",
            "sys_dict_item",
            "sys_config",
            "sys_feature_flag"
    ));

    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return new LongValue(DEFAULT_TENANT_ID);
        }
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return IGNORE_TABLES.contains(tableName);
    }
}
