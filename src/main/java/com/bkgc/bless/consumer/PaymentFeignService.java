package com.bkgc.bless.consumer;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.account.AuthAccount;
import com.bkgc.common.utils.RWrapper;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * Created by zhouyuzhao on 2018/4/16.
 */
@FeignClient("payment")
public interface PaymentFeignService {

    @RequestMapping(value = "/mchaccount/list", method = RequestMethod.POST)
    RWrapper<Map<String, AuthAccount>> getAccountList(@RequestBody Map<String, String> map);

    @RequestMapping(value = "/pay/bless/sendPackage", method = RequestMethod.POST)
    RWrapper<JSONObject> sendPackage(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/pay/bless/getPackage", method = RequestMethod.POST)
    RWrapper<JSONObject> getPackage(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/pay/pay4start", method = RequestMethod.POST)
    RWrapper<JSONObject> pay4start(@RequestBody JSONObject jsonObject);

    //获取个人账户  总资产，福金余额  ，可提现余额
    @RequestMapping(value = "/pay/getAccountInfo", method = RequestMethod.POST)
    RWrapper<JSONObject> getAccountInfo(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/pay/blessAward", method = RequestMethod.POST)
    RWrapper<JSONObject> blessAward(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/pay/bless/pay", method = RequestMethod.POST)
    RWrapper<JSONObject> blessPay(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/pay/doubleAward", method = RequestMethod.POST)
    RWrapper<JSONObject> doubleAward(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/pay/insertAuthAccount", method = RequestMethod.POST)
    RWrapper<JSONObject> insertAuthAccount(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/pay/bless/disCountCardPay", method = RequestMethod.POST)
    RWrapper<JSONObject> discountCardPay(@RequestBody JSONObject jsonObject);

    /**
     * 验证支付密码是否正确
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/pay/checkPaymentPassword", method = RequestMethod.POST)
    RWrapper<JSONObject> checkPaymentPassword(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/pay/queryFingerPayStatus", method = RequestMethod.POST)
    RWrapper<JSONObject> queryFingerPayStatus(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/pay/setFingerPayPwd", method = RequestMethod.POST)
    RWrapper<JSONObject> setFingerPayPwd(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/pay/checkFingerPayPwd", method = RequestMethod.POST)
    RWrapper<JSONObject> checkFingerPayPwd(@RequestBody JSONObject jsonObject);

    /**
     * 根据userId修改用户类型 用户类型(1个人2企业3.商户 8:福包天下用户 9：福包天下空间主)
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/updateUserType", method = RequestMethod.POST)
    RWrapper updateUserType(@RequestBody JSONObject jsonObject);

}
