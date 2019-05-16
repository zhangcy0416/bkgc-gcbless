package com.bkgc.game.jobhandler;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.game.RewardOfUser;
import com.bkgc.game.message.UserAppPushMessage;
import com.bkgc.game.service.RewardOfUserService;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>Title:      UserCardExpiredNotifyJobhandler </p>
 * <p>Description 道具卡过期提醒 </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author zhangft
 * @CreateDate 2018/7/11 下午2:18
 */
@JobHander(value = "userCardExpiredNotifyJobhandler")
@Service
@Slf4j
public class UserCardExpiredNotifyJobhandler extends IJobHandler {

    @Autowired
    private RewardOfUserService rewardOfUserService;

    @Autowired
    private UserAppPushMessage userAppPushMessage;


    @Override
    public ReturnT<String> execute(String... strings) throws Exception {

        //根据当前时间查询即将过期的卡包数据，给对应的用户发一条app通知消息
        //1.查询非福金，非实物的卡包，用户进行去重，可以将要过期的福包数量加进来
        //2.时间大于当前时间，小于当前时间+1天的数据

        XxlJobLogger.log("开始检查用户将要过期的福包卡");

        //当前时间减去过期时间分钟数，然后查询小于该时间的记录，返还金额
       /* Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar., -10);

        Date time = calendar.getTime();*/
        Date time = DateUtil.offsiteDay(new Date(), 1);


        List<RewardOfUser> rewardOfUsers = rewardOfUserService.selectExpireRewardList(time);

        for (RewardOfUser rewardOfUser : rewardOfUsers) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", rewardOfUser.getUserId());
            jsonObject.put("alert", "您有" + rewardOfUser.getRewardName() + "即将过期，请尽快使用");
            jsonObject.put("title", "过期通知");
            log.info("将要发送app消息，参数={}", jsonObject.toString());
            userAppPushMessage.pushUserCardMessage(jsonObject);
        }


        return ReturnT.SUCCESS;
    }

}
