package com.zhongjitang.common.core.context;

import lombok.Data;

/**
 * 租户上下文 - 通过ThreadLocal在请求线程内传递租户信息
 */
public class TenantContext {

    private static final ThreadLocal<TenantInfo> TENANT_HOLDER = new ThreadLocal<>();

    public static void set(TenantInfo tenantInfo) {
        TENANT_HOLDER.set(tenantInfo);
    }

    public static TenantInfo get() {
        return TENANT_HOLDER.get();
    }

    public static Long getTenantId() {
        TenantInfo info = TENANT_HOLDER.get();
        return info != null ? info.getTenantId() : null;
    }

    public static Long getStoreId() {
        TenantInfo info = TENANT_HOLDER.get();
        return info != null ? info.getStoreId() : null;
    }

    public static void clear() {
        TENANT_HOLDER.remove();
    }

    /**
     * 租户信息
     */
    @Data
    public static class TenantInfo {

        /**
         * 租户ID
         */
        private Long tenantId;

        /**
         * 当前门店ID（可为空，表示总部级别）
         */
        private Long storeId;

        /**
         * 套餐代码
         */
        private String planCode;

        public TenantInfo(Long tenantId, Long storeId, String planCode) {
            this.tenantId = tenantId;
            this.storeId = storeId;
            this.planCode = planCode;
        }
    }
}
