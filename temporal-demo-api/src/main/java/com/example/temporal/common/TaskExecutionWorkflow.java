package com.example.temporal.common;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow接口
 *
 * @author zhiwu.zzw
 */
@WorkflowInterface
public interface TaskExecutionWorkflow {

    /**
     * @param payload 任务具体的参数，建议用 JSON 字符串
     * @return 任务执行结果
     */
    @WorkflowMethod
    String executeTask(String payload);


}
