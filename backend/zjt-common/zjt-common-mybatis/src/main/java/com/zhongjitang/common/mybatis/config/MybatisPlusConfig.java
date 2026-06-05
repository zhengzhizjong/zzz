package com.zhongjitang.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.zhongjitang.common.core.context.TenantContext;
import com.zhongjitang.common.core.context.UserContext;
import com.zhongjitang.common.mybatis.handler.TenantLineHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis Plus配置
 * <p>
 * 配置分页插件、多租户插件、乐观锁插件和自动填充处理器。
 * </p>
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis Plus拦截器配置
     * <p>
     * 插件执行顺序：
     * 1. 多租户插件 - 自动追加租户条件
     * 2. 乐观锁插件 - 自动处理版本号
     * 3. 分页插件 - 自动处理分页
     * </p>
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 多租户插件
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new TenantLineHandlerImpl());
        interceptor.addInnerInterceptor(tenantInterceptor);

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;
    }

    /**
     * 自动填充处理器
     * <p>
     * 处理审计字段的自动填充：创建时间、更新时间、创建人、更新人、租户ID、门店ID、逻辑删除标记。
     * </p>
     *
     * @return MetaObjectHandler
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {

            /** 创建时间字段 */
            private static final String CREATED_AT = "createdAt";
            /** 更新时间字段 */
            private static final String UPDATED_AT = "updatedAt";
            /** 创建人字段 */
            private static final String CREATED_BY = "createdBy";
            /** 更新人字段 */
            private static final String UPDATED_BY = "updatedBy";
            /** 租户ID字段 */
            private static final String TENANT_ID = "tenantId";
            /** 门店ID字段 */
            private static final String STORE_ID = "storeId";
            /** 逻辑删除字段 */
            private static final String IS_DELETED = "isDeleted";

            /**
             * 插入时自动填充
             *
             * @param metaObject 元对象
             */
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                String currentUser = getCurrentUsername();

                this.strictInsertFill(metaObject, CREATED_AT, LocalDateTime.class, now);
                this.strictInsertFill(metaObject, UPDATED_AT, LocalDateTime.class, now);
                this.strictInsertFill(metaObject, CREATED_BY, String.class, currentUser);
                this.strictInsertFill(metaObject, UPDATED_BY, String.class, currentUser);
                this.strictInsertFill(metaObject, TENANT_ID, Long.class, TenantContext.getTenantId());
                this.strictInsertFill(metaObject, STORE_ID, Long.class, TenantContext.getStoreId());
                this.strictInsertFill(metaObject, IS_DELETED, Integer.class, 0);
            }

            /**
             * 更新时自动填充
             *
             * @param metaObject 元对象
             */
            @Override
            public void updateFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                String currentUser = getCurrentUsername();

                this.strictUpdateFill(metaObject, UPDATED_AT, LocalDateTime.class, now);
                this.strictUpdateFill(metaObject, UPDATED_BY, String.class, currentUser);
            }

            /**
             * 获取当前用户名
             *
             * @return 当前用户名，未登录时返回"system"
             */
            private String getCurrentUsername() {
                String username = UserContext.getUsername();
                return username != null ? username : "system";
            }
        };
    }
}
