package com.example.temporal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 任务状态
 *
 * @author zhiwu.zzw
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskStatus {

    private String taskId;          // 业务任务ID (对应 WorkflowId)
    private String runId;           // Temporal 的运行实例ID (唯一)
    private String status;          // 状态: RUNNING, SUCCESS, FAILED, TIMED_OUT
    private String result;          // 任务结果 (JSON 字符串)
    private String errorMessage;    // 失败原因

}
