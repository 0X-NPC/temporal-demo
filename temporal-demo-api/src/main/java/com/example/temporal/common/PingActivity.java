package com.example.temporal.common;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Ping
 *
 * @author 0xNPC
 */
@ActivityInterface
public interface PingActivity {

    @ActivityMethod
    String ping();

}
