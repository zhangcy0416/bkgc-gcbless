package com.bkgc.game.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.bkgc.bean.game.RewardFactor;
import com.bkgc.game.service.RewardFactorService;
import com.bkgc.game.service.impl.RewardFactorServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * <p>Title:      RewardFactorOfUserListener </p>
 * <p>Description 监听用户购彩和兑奖，更新游戏用户因子表 </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author         zhangft
 * @CreateDate     2018/7/19 下午3:13
 */
@Slf4j
public class RewardFactorOfUserListener implements MessageListener {


    @Override
    public Action consume(Message message, ConsumeContext context) {

        String str = new String(message.getBody());
        log.info("监听用户兑奖，参数={}", str);

        //{"amount":5,"tag":"ticket_award","userId":"d27310581b1745f382d0f4ad48deb628"}

        JSONObject jsonObject = JSON.parseObject(str);
        String userId = jsonObject.getString("userId");
        String tag = jsonObject.getString("tag");


        RewardFactorService rewardFactorService = RewardFactorServiceImpl.getInstance();

        RewardFactor rewardFactor = rewardFactorService.getByUserId(userId);


        if (rewardFactor != null && rewardFactor.getUserId() != null) {  //表中存在该用的记录,更新

            log.info("用户[" + userId + "]原有幸运值为：" + rewardFactor.getLuckyValue());

            BigDecimal amount = jsonObject.getBigDecimal("amount");
            if ("buy_ticket".equals(tag)) { //购买彩票: 更新购买彩票额度、更新幸运值

                rewardFactor.setPurchesLotteryAmount(rewardFactor.getPurchesLotteryAmount().add(amount));

                if (rewardFactor.getLuckyValue().intValue() < 100) {  //幸运值100以内增加
                    rewardFactor.setLuckyValue(rewardFactor.getLuckyValue() + 1);
                }
                rewardFactor.setLuckySum(rewardFactor.getLuckySum() + 1);

            } else if ("ticket_award".equals(tag)) {

                rewardFactor.setAwardAmount(rewardFactor.getAwardAmount().add(amount));

//                if (rewardFactor.getLuckyValue().intValue() < 100) {  //幸运值100以内增加
//                    rewardFactor.setLuckyValue(rewardFactor.getLuckyValue() + 1);
//                }
//                rewardFactor.setLuckySum(rewardFactor.getLuckySum() + 1);
            }

            log.info("用户[" + userId + "]最新的幸运值为：" + rewardFactor.getLuckyValue());

            rewardFactorService.updateByUserId(rewardFactor);

            log.info("更新完成");
        } else {  //表中不存在该用户记录,新增
            log.info("进入新增用户因子");

            BigDecimal amount = jsonObject.getBigDecimal("amount");

            log.info("兑奖金额={}", amount);

            try {

                rewardFactor = new RewardFactor();
                rewardFactor.setUserId(userId);

                if ("buy_ticket".equals(tag)) {
                    rewardFactor.setPurchesLotteryAmount(amount);
                } else if ("ticket_award".equals(tag)) {
                    rewardFactor.setAwardAmount(amount);
                }

                log.info("增加后兑奖金额={}", rewardFactor.getAwardAmount());

                rewardFactor.setLuckyValue(0);
                rewardFactor.setLuckySum(0);

                log.info("开始插入数据，参数={}", rewardFactor.toString());
                rewardFactorService.insertSelective(rewardFactor);

            }catch (Exception e){
                log.error("新增用户因子出现异常，异常信息={}", e.getMessage(), e);
            }

            log.info("新增完成");

        }
        return Action.CommitMessage;
    }

}
