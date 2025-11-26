package com.example.temporal.worker;


import com.example.temporal.common.TaskActivity;
import com.example.temporal.common.TaskExecutionWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

/**
 * Workflow的实现类
 *
 * @author zhiwu.zzw
 */
public class TaskExecutionWorkflowImpl implements TaskExecutionWorkflow {

    // 配置 Activity 的超时和重试
    private final TaskActivity activity = Workflow.newActivityStub(
            TaskActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(10)) // 单个任务最长执行时间
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3) // 失败重试 3 次
                            .build())
                    .build());

    @Override
    public String executeTask(String payload) {
        // 简单的透传：Workflow 收到 -> 调 Activity -> 返回结果
        return activity.runBusinessLogic(payload);
    }

}
