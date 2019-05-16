package com.bkgc.bless.config;

import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Properties;

/**
 * <p>Title:      MqInitializer </p>
 * <p>Description 消息队列初始化</p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author zhangft
 * @CreateDate 2017/6/23 上午9:43
 */
@Configuration
public class MqInitializer {
    @Resource
    private Properties producerProperties;
    @Resource
    private MqConfig mqConfig;

    @Value("${spring.profiles.active}")
    private String Env;

    private static String topic;

    /**
     * <p>Title:      初始化生产者 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2017/6/23 上午9:59
     */
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public Producer producer() {

        producerProperties.setProperty(PropertyKeyConst.ProducerId, mqConfig.getProducerId());
        producerProperties.setProperty(PropertyKeyConst.AccessKey, mqConfig.getAccessKey());
        producerProperties.setProperty(PropertyKeyConst.SecretKey, mqConfig.getSecretKey());
        producerProperties.setProperty(PropertyKeyConst.ONSAddr, mqConfig.getOnsAddr());
        Producer producer = ONSFactory.createProducer(producerProperties);

        return producer;
    }

    @PostConstruct
    public void initTopic() {
        topic = mqConfig.getTopic();
    }

    @Bean
    public Properties producerProperties() {
        return new Properties();
    }

    public static String getTopic() {
        return topic;
    }
}
