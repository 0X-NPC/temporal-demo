package com.example.temporal.worker;

import com.example.temporal.common.TaskActivity;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Activity的具体实现
 *
 * @author zhiwu.zzw
 */
@Slf4j
public class TaskActivityImpl implements TaskActivity {

    @Override
    public String runBusinessLogic(String payload) {
        log.info("Worker 收到任务: {}", payload);

        // 模拟执行业务逻辑
        try {
            // 解析 payload，执行 Shell 脚本或 SQL 等
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return "任务结果: " + System.currentTimeMillis();
    }

}
