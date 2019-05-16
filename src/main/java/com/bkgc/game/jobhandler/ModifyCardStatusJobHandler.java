package com.bkgc.game.jobhandler;

import com.bkgc.game.service.GameService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHander;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouyuzhao
 * @date 2018/7/12
 */
@JobHander(value = "modifyCardStatusJobHandler")
@Service
@Slf4j
public class ModifyCardStatusJobHandler extends IJobHandler {

    @Autowired
    private GameService gameService;

    @Override
    public ReturnT<String> execute(String... strings) throws Exception {
        log.info("开始修改过期卡的状态!");
        gameService.updateOverdueCardStatus();
        log.info("修改状态完成!");
        return ReturnT.SUCCESS;
    }
}
