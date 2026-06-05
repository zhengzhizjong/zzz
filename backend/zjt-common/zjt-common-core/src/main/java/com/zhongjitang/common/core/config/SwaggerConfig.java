package com.zhongjitang.common.core.config;

import com.zhongjitang.common.core.result.PageResult;
import com.zhongjitang.common.core.result.R;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI配置
 * <p>
 * 配置API文档基本信息、JWT安全认证方案和全局响应模型。
 * </p>
 */
@Configuration
public class SwaggerConfig {

    /** 安全方案名称 */
    private static final String SECURITY_SCHEME_NAME = "Bearer JWT";

    /**
     * 配置OpenAPI
     *
     * @return OpenAPI对象
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("忠济堂API")
                        .description("忠济堂中医养生连锁管理系统接口文档")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("请输入JWT Token"))
                        .addSchemas("R", createSchema(R.class))
                        .addSchemas("PageResult", createSchema(PageResult.class)));
    }

    /**
     * 创建Schema
     * <p>
     * 根据Class创建OpenAPI Schema，用于全局响应模型注册。
     * </p>
     *
     * @param clazz 类对象
     * @return Schema对象
     */
    private io.swagger.v3.oas.models.media.Schema<?> createSchema(Class<?> clazz) {
        return new io.swagger.v3.oas.models.media.Schema<>()
                .type("object")
                .name(clazz.getSimpleName());
    }
}
