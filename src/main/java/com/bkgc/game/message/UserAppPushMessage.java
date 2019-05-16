package com.bkgc.game.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.consumer.MessageFeignService;
import com.bkgc.common.exception.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <p>Title:      UserAppPushMessage </p>
 * <p>Description 推送福包卡过期提醒消息 </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author zhangft
 * @CreateDate 2018/7/11 下午4:05
 */
@Service
public class UserAppPushMessage {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MessageFeignService messageFeignService;


    public boolean pushUserCardMessage(JSONObject jsonObject) {
        Map<String, String> paramMap = new HashMap<>();
        //通知内容
        paramMap.put("alert", jsonObject.getString("alert"));

        paramMap.put("msgContent", paramMap.get("alert"));

        //别名
        paramMap.put("alias", jsonObject.getString("userId"));
        //是否可点击进入
        paramMap.put("clickIn", "0");
        //消息类型
        paramMap.put("msgType", "1001");
        //停留时长
        paramMap.put("remainTimes", "15");
        //是否重复显示
        paramMap.put("repeatShow", "0");
        //显示优先级
        paramMap.put("showPriority", "1");
        //链接地址
        paramMap.put("url", "userCard");

        paramMap.put("title", jsonObject.getString("title"));

        //加签
        paramMap.put("nonce_str", UUID.randomUUID().toString());

        log.info("发送消息数据{}", paramMap.toString());

        JSONObject retJson = messageFeignService.sendByAlias(JSONObject.parseObject(JSON.toJSONString(paramMap))).getData();
        log.info("发送消息返回数据{}", retJson);
        if (ResultCodeEnum.SUCCESS.getCode().equals(retJson.getString("code"))) {
            log.info("发送消息成功");
            return true;
        } else {
            return false;
        }
    }

}
