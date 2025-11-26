package com.example.temporal.server.service;

import com.example.temporal.common.PingWorkflow;
import com.example.temporal.common.TaskWorkflow;
import com.example.temporal.model.TaskArgs;
import com.example.temporal.model.TaskStatus;
import com.example.temporal.server.constants.TaskType;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.internal.worker.WorkflowExecutionException;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TaskDispatchService {

    private final WorkflowClient workflowClient;

    /**
     * 1. 下发任务（支持同步/异步）
     *
     * @param taskType 任务类型 (SYNC/ASYNC)
     * @param region   目标区域 (Queue Name)
     * @param taskId   业务任务ID (WorkflowId)
     * @param command  业务指令
     * @return 如果是 ASYNC，返回 RunId；如果是 SYNC，返回任务的执行结果
     */
    public String dispatchTask(TaskType taskType, String region, String taskId, String command) {
        // 1. 构建 Workflow 配置
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(region)               // 核心：路由到指定区域
                .setWorkflowId(taskId)              // 核心：业务ID去重
                // 策略建议：仅允许在上一条相同ID的任务 失败/超时/终止 后，才允许复用ID。
                // 如果上一条还在运行，这里会报错 (WorkflowExecutionAlreadyStarted)
                .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY)
                .build();
        // 2. 准备参数
        TaskArgs args = TaskArgs.builder().build();

        // 3. 根据类型分发
        if (TaskType.SYNC == taskType) {
            // 同步模式
            // 创建 Workflow 存根 (Client Stub)
            PingWorkflow syncWorkflow = workflowClient.newWorkflowStub(
                    PingWorkflow.class, options
            );
            return dispatchSync(syncWorkflow, region, taskId, command, args);
        } else {
            // 异步模式
            TaskWorkflow workflow = workflowClient.newWorkflowStub(
                    TaskWorkflow.class, options
            );
            return dispatchAsync(workflow, region, taskId, command, args);
        }
    }

    /**
     * 处理异步任务 (Fire and Forget)
     */
    private String dispatchAsync(TaskWorkflow workflow, String region, String taskId, String command, TaskArgs args) {
        try {
            // WorkflowClient.start 是异步非阻塞的，发送成功即返回
            WorkflowExecution execution = WorkflowClient.start(
                    workflow::executeTask,
                    command,
                    args
            );

            log.info("[ASYNC] 任务已下发: Region={}, ID={}, RunID={}", region, taskId, execution.getRunId());
            // 异步模式返回 RunID，方便调用方后续查询状态
            return execution.getRunId();

        } catch (Exception e) {
            log.error("[ASYNC] 任务下发失败: Region={}, ID={}", region, taskId, e);
            throw new RuntimeException("异步任务下发失败", e);
        }
    }

    /**
     * 处理同步任务 (Blocking Wait)
     */
    private String dispatchSync(PingWorkflow workflow, String region, String taskId, String command, TaskArgs args) {
        log.info("[SYNC] 开始同步调用: Region={}, ID={}", region, taskId);
        Long startTime = System.currentTimeMillis();
        try {
            // 直接调用接口方法，会阻塞当前线程，直到 Workflow 执行完毕
            // 如果 Workflow 执行失败，这里会抛出 WorkflowException
            String result = workflow.executeTask(command, args);

            long costTime = System.currentTimeMillis() - startTime;
            log.info("[SYNC] 任务执行完成: Region={}, ID={}, Result={}, Cost={}ms", region, taskId, result, costTime);
            // 同步模式直接返回业务结果
            return result;

        } catch (WorkflowExecutionException e) {
            log.error("[SYNC] 任务执行异常: Region={}, ID={}", region, taskId, e);
            throw new RuntimeException("同步任务执行失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[SYNC] 调用系统异常: Region={}, ID={}", region, taskId, e);
            throw new RuntimeException("同步任务系统异常", e);
        }
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
            WorkflowStub stub = workflowClient.newUntypedWorkflowStub(taskId, Optional.empty(), Optional.empty());

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
        TaskWorkflow workflow = workflowClient.newWorkflowStub(TaskWorkflow.class, taskId);
        // 这会阻塞直到任务完成
        return workflow.executeTask(null, TaskArgs.builder().build());
    }

}
