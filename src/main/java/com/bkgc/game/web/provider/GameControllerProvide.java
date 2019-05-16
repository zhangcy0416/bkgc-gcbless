package com.bkgc.game.web.provider;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.game.MailRewardOrder;
import com.bkgc.bean.game.MailedAward;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.WrapperUtil;
import com.bkgc.game.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by gmg on on 2018-01-03 17:02.
 */
@Slf4j
@RestController
@RequestMapping(value = "/gameProvider")
public class GameControllerProvide {

    @Autowired
    GameService gameService;

    @RequestMapping(value = "/getMailRewardOrder")
    public RWrapper<Map<String, Object>> getMailRewardOrder(@RequestBody MailRewardOrder mailRewardOrder) {
        return WrapperUtil.ok(gameService.getmailRewardOrderList(mailRewardOrder));
    }

    @RequestMapping(value = "/updateMailRewardOrder")
    public RWrapper updateMailRewardOrder(@RequestBody MailedAward mailedAward) {
        return gameService.updateByPrimaryKeySelective(mailedAward);
    }

    /**
     * <p>Title:      查询游戏福包情况 </p>
     * <p>Description  </p>
     *
     * @Author        zhangft
     * @CreateDate    2018/11/14 上午10:33
     */
    @RequestMapping(value = "/queryGameBless")
    public RWrapper<JSONObject> queryGameBless(@RequestBody JSONObject params){

        log.info("进入查询游戏福包情况数据接口，参数={}", params.toString());

        String date = params.getString("date");

        JSONObject data = gameService.queryGameBless(date);

        log.info("查询结果={}", data.toString());

        return WrapperUtil.ok(data);
    }


}
