package com.example.temporal.server.config;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;

/**
 * Temporal服务配置类
 *
 * @author 0xNPC
 */
@Configuration
@EnableConfigurationProperties(TemporalProperties.class)
public class TemporalConfig {

    @Bean
    public WorkflowClient workflowClient(TemporalProperties temporalProperties) throws SSLException {
        String frontendAddress = temporalProperties.getFrontendAddress();
        // 连接 Temporal Server
        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        // Temporal Frontend 地址，gRPC内部端口是7233
                        .setTarget(frontendAddress)
                        // 2. 如果是自签名证书，使用非验证模式
                        .setSslContext(GrpcSslContexts.forClient()
                                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                .build()
                        )
                        .build());
        return WorkflowClient.newInstance(service);
    }

}
