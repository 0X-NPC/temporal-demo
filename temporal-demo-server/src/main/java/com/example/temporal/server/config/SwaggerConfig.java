package com.example.temporal.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

/**
 * swagger-ui接口文档配置，openapi-v3
 *
 * @author 0xNPC
 */
@Configuration
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", matchIfMissing = false)
public class SwaggerConfig {

    @Bean
    public OpenAPI openApi() {
        // @formatter:off
          return new OpenAPI().info(new Info()
                  .title("Temporal Demo Server")
                  .version("1.0.0")
          );
          // @formatter:on
    }

    /**
     * swagger-ui接口安全认证头配置
     *
     * @return
     */
    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> openApi.getPaths().forEach((s, pathItem) -> {
            pathItem.readOperations().forEach(operation -> {
                operation.addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION));
            });
        });
    }

    /**
     * 为每个接口生成唯一ID，避免方法重载时出现ID重复问题
     * <p>
     * springdoc-openapi v2.8.9版本还能重现「operationId重复」这个问题
     *
     * @return
     */
    @Bean
    public OperationCustomizer operationIdCustomizer() {
        return (operation, handlerMethod) -> {
            // 使用类名+方法名组合生成唯一ID，避免重载方法ID冲突
            String className = handlerMethod.getMethod().getDeclaringClass().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            operation.setOperationId(className + "." + methodName);
            return operation;
        };
    }

}
