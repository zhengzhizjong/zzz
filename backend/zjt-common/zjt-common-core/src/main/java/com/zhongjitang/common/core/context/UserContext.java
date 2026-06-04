package com.zhongjitang.common.core.context;

import lombok.Data;

/**
 * 用户上下文 - 通过ThreadLocal在请求线程内传递用户信息
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    public static void set(UserInfo userInfo) {
        USER_HOLDER.set(userInfo);
    }

    public static UserInfo get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        UserInfo info = USER_HOLDER.get();
        return info != null ? info.getUserId() : null;
    }

    public static String getUsername() {
        UserInfo info = USER_HOLDER.get();
        return info != null ? info.getUsername() : null;
    }

    public static String getUserType() {
        UserInfo info = USER_HOLDER.get();
        return info != null ? info.getUserType() : null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    /**
     * 用户信息
     */
    @Data
    public static class UserInfo {

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 用户类型: member/employee/admin
         */
        private String userType;

        /**
         * 关联门店ID
         */
        private Long storeId;

        public UserInfo(Long userId, String username, String userType, Long storeId) {
            this.userId = userId;
            this.username = username;
            this.userType = userType;
            this.storeId = storeId;
        }
    }
}
