package com.example.temporal.task;

import com.example.temporal.common.TaskActivity;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Activity的具体实现
 *
 * @author 0xNPC
 */
@Slf4j
public class TaskActivityImpl implements TaskActivity {

    @Override
    public String runBusinessLogic(String payload) {
        log.info("Worker 收到任务: {}", payload);
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            log.error("任务执行异常: {}", e.getMessage(), e);
        }
        return "任务结果: " + System.currentTimeMillis();
    }

}
