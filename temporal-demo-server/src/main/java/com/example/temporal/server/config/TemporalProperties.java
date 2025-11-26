package com.example.temporal.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.example.temporal.server.config.TemporalProperties.PREFIX;

/**
 * Temporal参数配置
 *
 * @author 0xNPC
 */
@Data
@ConfigurationProperties(prefix = PREFIX)
public class TemporalProperties {

    public static final String PREFIX = "temporal";

    /**
     * Temporal Frontend 地址
     */
    private String frontendAddress = "127.0.0.1:7233";

}
