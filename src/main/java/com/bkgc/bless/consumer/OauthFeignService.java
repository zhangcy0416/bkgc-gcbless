package com.bkgc.bless.consumer;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.common.utils.RWrapper;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by zhouyuzhao on 2018/4/16.
 */
@FeignClient("auth")
public interface OauthFeignService {

    /*@RequestMapping(value = "/oauth/getByUnionId", method = RequestMethod.POST)
    RWrapper<JSONObject> getByUnionId(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/oauth/registerWithUnionId", method = RequestMethod.POST)
    RWrapper<JSONObject> registerWithUnionId(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/oauth/bindUnionId", method = RequestMethod.POST)
    RWrapper<JSONObject> bindUnionId(@RequestBody JSONObject jsonObject);*/
}
