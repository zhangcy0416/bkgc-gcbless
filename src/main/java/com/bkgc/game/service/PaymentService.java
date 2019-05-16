package com.bkgc.game.service;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.account.AccountWithdraw;
import com.bkgc.common.utils.RWrapper;
import com.github.pagehelper.PageInfo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;


@FeignClient(value = "payment")
public interface PaymentService {

    @RequestMapping(value = "/payinfo/withdrawRecord")
    RWrapper<PageInfo<AccountWithdraw>> getWithdrawList(@RequestBody JSONObject jsonObject);


    //开始游戏  扣福包账户5元
    @RequestMapping(value = "/gamepay/pay4start", method = RequestMethod.POST)
    RWrapper startGame(Map<String, Object> gameParams);

    //折扣卡使用
    @RequestMapping(value = "/xxx/xxx")
    RWrapper useDiscountCard(Map<String, Object> disCountParams);


    //翻倍卡使用
    @RequestMapping(value = "/gamepay/doubleAward")
    RWrapper useAwardCard(Map<String, Object> awardParams);

    //获得福包使用
    @RequestMapping(value = "/gamepay/blessAward")
    RWrapper useBlessEnve(Map<String, Object> awardParams);


}