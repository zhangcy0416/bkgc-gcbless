package com.bkgc.bless.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Data
@Configuration
@ConfigurationProperties(prefix = MqConfig.MQ_PREFIX)
public class MqConfig {

    public final static String MQ_PREFIX = "mq";

    @NotNull
    private String producerId;
    @NotNull
    private String consumerId;
    @NotNull
    private String accessKey;
    @NotNull
    private String secretKey;
    @NotNull
    private String topic;
    /**
     * ONSADDR 请根据不同Region进行配置
     * 公网测试: http://onsaddr-internet.aliyun.com/rocketmq/nsaddr4client-internet
     * 公有云生产: http://onsaddr-internal.aliyun.com:8080/rocketmq/nsaddr4client-internal
     */
    @NotNull
    private String onsAddr;

}
