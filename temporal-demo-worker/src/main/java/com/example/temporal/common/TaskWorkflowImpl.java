package com.example.temporal.common;


import com.example.temporal.model.TaskArgs;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

/**
 * Workflow的实现类
 *
 * @author 0xNPC
 */
public class TaskWorkflowImpl implements TaskWorkflow {

    @Override
    public String executeTask(String payload, TaskArgs taskArgs) {
        Duration executionTimeout = taskArgs.getExecutionTimeout() == null
                ? Duration.ofMinutes(10) : taskArgs.getExecutionTimeout();
        Integer retryCount = taskArgs.getRetryCount() == null ? 1 : taskArgs.getRetryCount();

        TaskActivity activity = Workflow.newActivityStub(
                TaskActivity.class,
                ActivityOptions.newBuilder()
                        // 单个任务最长执行时间
                        .setStartToCloseTimeout(executionTimeout)
                        .setRetryOptions(RetryOptions.newBuilder()
                                // 失败重试
                                .setMaximumAttempts(retryCount)
                                .build())
                        .build());
        // 简单的透传：Workflow 收到 -> 调 Activity -> 返回结果
        return activity.runBusinessLogic(payload);
    }

}
