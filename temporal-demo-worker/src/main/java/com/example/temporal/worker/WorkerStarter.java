package com.example.temporal.worker;

import com.example.temporal.common.TaskWorkflowImpl;
import com.example.temporal.common.PingWorkflowImpl;
import com.example.temporal.task.PingActivityImpl;
import com.example.temporal.task.TaskActivityImpl;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;

/**
 * Worker启动类
 *
 * @author 0xNPC
 */
@Slf4j
public class WorkerStarter {

    public static void main(String[] args) throws SSLException {
        String temporalAddress = "temporal-frontend.local.ht:443";

        // 1. 配置连接到公网/专线上的 Temporal Server
        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        // Temporal Server 地址
                        .setTarget(temporalAddress)
                        // 自签名证书，使用非验证模式
                        .setSslContext(GrpcSslContexts.forClient()
                                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                .build()
                        )
                        .build());
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);

        // 2. 关键：指定该 Worker 属于哪个区域 (Queue Name)
        // 比如部署在北京机房，就叫 "queue-beijing"
        Worker worker = factory.newWorker("queue-beijing");

        // 3. 注册实现类
        // 负责编排，控制任务，不做具体任务的执行
        // 注意: 也可以设计到独立的worker中进行部署，不与执行的worker合并部署
        worker.registerWorkflowImplementationTypes(TaskWorkflowImpl.class);
        worker.registerWorkflowImplementationTypes(PingWorkflowImpl.class);
        // 负责具体任务的最终执行
        worker.registerActivitiesImplementations(new TaskActivityImpl());
        worker.registerActivitiesImplementations(new PingActivityImpl());

        // 4. 启动
        factory.start();
        log.info("北京区域 Worker 已启动，等待任务...");
    }

}
