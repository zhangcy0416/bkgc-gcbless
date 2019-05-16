package com.bkgc.game.service;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.award.LottoAutoawardOrder;
import com.bkgc.common.utils.RWrapper;
import com.github.pagehelper.PageInfo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author gmg on
 * @date 2017-12-26 10:56
 */
@FeignClient("award")
public interface AwardService {

    /**
     * 获取未使用奖金翻倍卡且在有效时间段内的订单列表使用
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/award/game/queryAwardOrderList")
    RWrapper<PageInfo<LottoAutoawardOrder>> getValidAwardList(JSONObject jsonObject);
}
