package com.bkgc.game.config;

import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.bkgc.game.mq.consumer.RewardFactorOfUserListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Properties;

/**
 * <p>Title:      RewardMQInitializer </p>
 * <p>Description  </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author         zhangft
 * @CreateDate     2018/7/19 下午3:18
 */
@Configuration
@Slf4j
public class RewardMQInitializer {

    @Resource
    private RewardMQConfig rewardMQConfig;


    @Value("${spring.profiles.active}")
    private String Env;


    /**
     * 消费用户游戏获奖规则影响因子数据
     * @return
     */
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public Consumer consumer() {

        Properties properties = new Properties();

        // 您在MQ控制台创建的Consumer ID
        properties.put(PropertyKeyConst.ConsumerId, "CID_Reward_Factor_New_" + this.Env);
        log.info("CID={}", properties.getProperty(PropertyKeyConst.ConsumerId));
        // 鉴权用AccessKey，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.AccessKey, rewardMQConfig.getAccessKey().trim());
        log.info("AK={}", properties.getProperty(PropertyKeyConst.AccessKey));
        // 鉴权用SecretKey，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.SecretKey, rewardMQConfig.getSecretKey().trim());
        log.info("SK={}", properties.getProperty(PropertyKeyConst.SecretKey));
        // 设置 TCP 接入域名（此处以公共云公网环境接入为例）
        properties.put(PropertyKeyConst.ONSAddr, rewardMQConfig.getOnsAddr());
        log.info("ADDR={}", properties.getProperty(PropertyKeyConst.ONSAddr));

        Consumer consumer = ONSFactory.createConsumer(properties);
        consumer.subscribe(this.Env + "_topic_bkgc_reward_factor_new", "*", new RewardFactorOfUserListener());
        log.info("TOPIC={}", this.Env + "_topic_bkgc_reward_factor_new");

        return consumer;
    }

}
