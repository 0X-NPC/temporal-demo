package com.example.temporal.common;

import com.example.temporal.model.TaskArgs;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow接口
 *
 * @author 0xNPC
 */
@WorkflowInterface
public interface TaskWorkflow {

    /**
     * @param payload 任务具体的参数
     * @param args    任务控制参数
     * @return 任务执行结果
     */
    @WorkflowMethod
    String executeTask(String payload, TaskArgs args);


}
