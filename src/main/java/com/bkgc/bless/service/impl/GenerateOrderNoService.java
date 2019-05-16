package com.bkgc.bless.service.impl;

import com.bkgc.bless.config.Config;
import com.bkgc.common.utils.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>Title:      GenerateOrderNoService </p>
 * <p>Description 订单号生成策略 </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author zhangft
 * @CreateDate 2018/8/29 下午2:20
 */
@Service
@Slf4j
public class GenerateOrderNoService {

    @Resource
    private Config config;


    public String generate() {

        SnowflakeIdWorker snowflakeIdWorker = SnowflakeIdWorker.getInstance(config.getWorkerId(), 4);

        log.info("对象={}", snowflakeIdWorker);

        long id = snowflakeIdWorker.nextId();

        log.info("生成的id={}", id);

        return String.valueOf(id);
    }

}
