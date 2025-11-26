package com.example.temporal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * 任务控制参数
 *
 * @author 0xNPC
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskArgs {

    /**
     * 单个任务最长执行时间（毫秒）
     */
    private Duration executionTimeout = Duration.ofMinutes(60);

    /**
     * 失败重试次数，设置为1表示不重试
     */
    private Integer retryCount = 1;


}
