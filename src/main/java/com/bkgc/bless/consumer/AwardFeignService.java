package com.bkgc.bless.consumer;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.award.LottoAutoawardOrder;
import com.bkgc.bless.consumer.hystric.AwardFeignServiceHystric;
import com.bkgc.common.utils.RWrapper;
import com.github.pagehelper.PageInfo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "award", fallback = AwardFeignServiceHystric.class)
public interface AwardFeignService {

    @RequestMapping(value = "/awardorder/lottoCodeUpload4AutoAward", method = RequestMethod.POST)
    RWrapper<JSONObject> lottoCodeUpload4AutoAward(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/awardorder/autoAwardStatusQuery", method = RequestMethod.POST)
    RWrapper<LottoAutoawardOrder> autoAwardStatusQuery(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/awardorder/awardRecordsQuery", method = RequestMethod.POST)
    RWrapper<PageInfo<LottoAutoawardOrder>> awardRecordsQuery(@RequestBody JSONObject paramJson);

}
