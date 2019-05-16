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
@FeignClient("fbtx")
public interface FbtxFeignService {

    /**
     * 空间主拥有的空间数
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/spaceInfo/spaceNum", method = RequestMethod.POST)
    RWrapper<JSONObject> spaceNum(@RequestBody JSONObject jsonObject);

}
