package com.example.temporal.server.controller;

import com.example.temporal.model.TaskStatus;
import com.example.temporal.server.controller.vo.request.TaskReq;
import com.example.temporal.server.service.TaskDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Temporal Controller
 *
 * @author 0xNPC
 */
@Slf4j
@Tag(name = "Temporal")
@RestController
@RequestMapping("/temporal")
@RequiredArgsConstructor
public class TemporalController {

    private final TaskDispatchService taskDispatchService;

    @Operation(summary = "启动任务", description = "提供指定的任务")
    @PostMapping(value = "/")
    public ResponseEntity<String> runTask(@Valid @RequestBody TaskReq taskReq) {
        String taskId = UUID.randomUUID().toString();
        taskDispatchService.dispatchTask(taskReq.getRegion(), taskId, taskReq.getCommand());
        return ResponseEntity.ok(taskId);
    }


    @Operation(summary = "任务状态", description = "查询任务状态")
    @GetMapping(value = "/{taskId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskStatus>  getTaskStatus(@Valid @PathVariable(name = "taskId") String taskId) {
        TaskStatus taskStatus = taskDispatchService.checkStatus(taskId);
        return ResponseEntity.ok(taskStatus);
    }


}
