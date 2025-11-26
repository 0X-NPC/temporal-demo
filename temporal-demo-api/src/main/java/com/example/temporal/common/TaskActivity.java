package com.example.temporal.common;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activity接口 (实际执行实现)
 *
 * @author zhiwu.zzw
 */
@ActivityInterface
public interface TaskActivity {

    @ActivityMethod
    String runBusinessLogic(String payload);

}
