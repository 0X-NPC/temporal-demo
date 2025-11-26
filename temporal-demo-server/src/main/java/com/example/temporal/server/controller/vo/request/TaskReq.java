package com.example.temporal.server.controller.vo.request;

import com.example.temporal.server.constants.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 启动任务请求
 *
 * @author 0xNPC
 */
@Data
@Schema(title = "启动任务请求")
public class TaskReq {

    @Schema(title = "Region", defaultValue = "queue-beijing")
    private String region;

    @Schema(title = "命令")
    private String command;

    @Schema(title = "任务类型", defaultValue = "ASYNC")
    private TaskType taskType = TaskType.ASYNC;

}
