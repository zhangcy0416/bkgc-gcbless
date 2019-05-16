package com.bkgc.bless.consumer.hystric;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.SearchBean;
import com.bkgc.bean.award.LottoAutoawardOrder;
import com.bkgc.bless.consumer.AwardFeignService;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.WrapperUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>Title:      AwardFeignServiceHystric </p>
 * <p>Description 熔断器 </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author         zhangft
 * @CreateDate     2018/4/27 下午5:17
 */
@Component
public class AwardFeignServiceHystric implements AwardFeignService {

    @Override
    public RWrapper<JSONObject> lottoCodeUpload4AutoAward(@RequestBody JSONObject jsonObject) {
        return WrapperUtil.error(ResultCodeEnum.SERVICE_BUSY);
    }

    @Override
    public RWrapper<LottoAutoawardOrder> autoAwardStatusQuery(@RequestBody JSONObject jsonObject) {
        return WrapperUtil.error(ResultCodeEnum.SERVICE_BUSY);
    }

    @Override
    public RWrapper<PageInfo<LottoAutoawardOrder>> awardRecordsQuery(@RequestBody JSONObject paramJson) {
        return WrapperUtil.error(ResultCodeEnum.SERVICE_BUSY);
    }

}
