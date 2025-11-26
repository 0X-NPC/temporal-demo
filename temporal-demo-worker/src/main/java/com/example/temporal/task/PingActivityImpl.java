package com.example.temporal.task;

import com.example.temporal.common.PingActivity;
import lombok.extern.slf4j.Slf4j;

/**
 * Ping Activity的具体实现，模拟同步调用方式不sleep
 *
 * @author 0xNPC
 */
@Slf4j
public class PingActivityImpl implements PingActivity {

    @Override
    public String ping() {
        log.info("Worker 收到 Ping");
        return "Pong: " + System.currentTimeMillis();
    }

}
