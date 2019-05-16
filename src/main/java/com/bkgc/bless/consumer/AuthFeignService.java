package com.bkgc.bless.consumer;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.common.utils.RWrapper;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <p>Title:      AuthFeignService </p>
 * <p>Description 认证注册相关 </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author zhangft
 * @CreateDate 2018/5/28 下午2:54
 */
@FeignClient("auth")
public interface AuthFeignService {

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    RWrapper<JSONObject> register(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/user/deleteByUserName", method = RequestMethod.POST)
    RWrapper deleteByUserName(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/user/getUserInfoByPhone", method = RequestMethod.POST)
    RWrapper<JSONObject> getUserInfoByPhone(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/user/get", method = RequestMethod.POST)
    RWrapper<JSONObject> userGet(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/user/updateUserInfo", method = RequestMethod.POST)
    RWrapper<JSONObject> updateUserInfo(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/oauth/getByUnionId", method = RequestMethod.POST)
    RWrapper<JSONObject> getByUnionId(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/oauth/registerWithUnionId", method = RequestMethod.POST)
    RWrapper<JSONObject> registerWithUnionId(@RequestBody JSONObject jsonObject);

    @RequestMapping(value = "/oauth/bindUnionId", method = RequestMethod.POST)
    RWrapper<JSONObject> bindUnionId(@RequestBody JSONObject jsonObject);

    /**
     * 升级为空间主
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/disableUser/upgrade", method = RequestMethod.POST)
    RWrapper upgrade(@RequestBody JSONObject jsonObject);

    /**
     * 修改状态
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/disableUser/updateStatus", method = RequestMethod.POST)
    RWrapper updateStatus(@RequestBody JSONObject jsonObject);


    @RequestMapping(value = "/checkIdCard/check", method = RequestMethod.POST)
    RWrapper checkIdCardCheck(@RequestBody JSONObject jsonObject);


}
