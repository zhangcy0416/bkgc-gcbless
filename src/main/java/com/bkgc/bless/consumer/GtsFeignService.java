package com.bkgc.bless.consumer;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.common.utils.RWrapper;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * @author zhouyuzhao
 */
@FeignClient("gts")
public interface GtsFeignService {

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    RWrapper<JSONObject> register(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/game/start", method = RequestMethod.POST)
    RWrapper<JSONObject> gameStart(@RequestBody JSONObject jsonObject);

    /**
     * 奖金翻倍
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/reward/rewardMultiple", method = RequestMethod.POST)
    RWrapper<JSONObject> rewardMultiple(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/game/useDisCountCard", method = RequestMethod.POST)
    RWrapper<JSONObject> useDisCountCard(@RequestBody JSONObject jsonObject);

}
