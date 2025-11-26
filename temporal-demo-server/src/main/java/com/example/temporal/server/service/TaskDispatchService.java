package com.example.temporal.server.service;

import com.example.temporal.common.TaskExecutionWorkflow;
import com.example.temporal.model.TaskStatus;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Dispatch Server实现
 *
 * @author 0xNPC
 */
@Slf4j
@Service
public class TaskDispatchService {

    private WorkflowClient client;

    @PostConstruct
    public void init() {
        String temporalAddress = "127.0.0.1:7233";
        // 连接 Temporal Server
        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        // K8s 内部地址
                        .setTarget(temporalAddress)
                        .build());
        this.client = WorkflowClient.newInstance(service);
    }

    /**
     * 1. 任务下发 (Dispatch)
     *
     * @param region  目标区域，对应 Queue Name (e.g., "queue-beijing")
     * @param taskId  原有调度系统的任务ID (用于去重和追踪)
     * @param command 具体的任务参数
     * @return runId Temporal 的运行ID
     */
    public String dispatchTask(String region, String taskId, String command) {
        // 创建 Workflow 存根
        TaskExecutionWorkflow workflow = client.newWorkflowStub(
                TaskExecutionWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(region)   // 核心：通过 Queue 路由到指定隔离网络
                        .setWorkflowId(taskId)  // 核心：使用业务ID作为 WorkflowId，防止重复下发
                        .build());

        // 异步启动 (Fire and Forget)
        // 调度服务通常不希望阻塞等待任务结束
        WorkflowExecution execution = WorkflowClient.start(workflow::executeTask, command);

        log.info("任务已下发: Region={}, ID={}, RunID={}", region, taskId, execution.getRunId());
        return execution.getRunId();
    }

    /**
     * 2. 状态回收 (Status Collection)
     * 调度服务可以轮询调用此方法来检查任务是否完成
     */
    /**
     * 查询任务状态 (非阻塞)
     *
     * @param taskId 你的业务任务ID (WorkflowId)
     * @return TaskStatus 对象
     */
    public TaskStatus checkStatus(String taskId) {
        try {
            // 1. 创建无类型的 Stub (UntypedStub)
            // 这是一个轻量级对象，用于操作已存在的 Workflow
            WorkflowStub stub = client.newUntypedWorkflowStub(taskId, Optional.empty(), Optional.empty());

            // 2. 调用 Describe 获取元数据 (这是一个 RPC 请求)
            // 注意：如果 WorkflowId 不存在，这里会抛出异常
            WorkflowExecutionInfo executionInfo = stub.describe().getWorkflowExecutionInfo();
            WorkflowExecutionStatus temporalStatus = executionInfo.getStatus();

            // 3. 构建基础状态对象
            TaskStatus status = new TaskStatus();
            status.setTaskId(taskId);
            status.setRunId(executionInfo.getExecution().getRunId());

            // 4. 状态映射与结果提取
            switch (temporalStatus) {
                case WORKFLOW_EXECUTION_STATUS_RUNNING:
                    status.setStatus("RUNNING");
                    break;

                case WORKFLOW_EXECUTION_STATUS_COMPLETED:
                    status.setStatus("SUCCESS");
                    // 只有成功时，才去获取结果
                    // getResult(Class) 在已完成的任务上调用是立即返回的，不会阻塞
                    String resultPayload = stub.getResult(String.class);
                    status.setResult(resultPayload);
                    break;

                case WORKFLOW_EXECUTION_STATUS_FAILED:
                    status.setStatus("FAILED");
                    // 获取失败原因较复杂，通常通过 getResult 捕获异常来拿
                    try {
                        stub.getResult(String.class);
                    } catch (Exception e) {
                        status.setErrorMessage(e.getMessage());
                    }
                    break;

                case WORKFLOW_EXECUTION_STATUS_TIMED_OUT:
                    status.setStatus("TIMEOUT");
                    status.setErrorMessage("Task execution timed out.");
                    break;

                case WORKFLOW_EXECUTION_STATUS_CANCELED:
                    status.setStatus("CANCELED");
                    break;

                case WORKFLOW_EXECUTION_STATUS_TERMINATED:
                    status.setStatus("KILLED"); // 被人工强制终止
                    break;

                default:
                    status.setStatus("UNKNOWN");
            }

            return status;

        } catch (Exception e) {
            // 如果 ID 不存在，或者连接不上 Temporal
            return new TaskStatus(taskId, null, "NOT_FOUND", null, e.getMessage());
        }
    }

    /**
     * 同步获取任务状态方式（阻塞等待或带超时）
     *
     * @param taskId
     * @return
     */
    public String getResultSync(String taskId) {
        TaskExecutionWorkflow workflow = client.newWorkflowStub(TaskExecutionWorkflow.class, taskId);
        // 这会阻塞直到任务完成
        return workflow.executeTask(null);
    }

}
