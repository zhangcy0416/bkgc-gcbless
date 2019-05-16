package com.bkgc.bless.web.provider;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.service.impl.AuthService;
import com.bkgc.bless.web.controller.AuthController;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.WrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zhouyuzhao on 2018/4/23.
 */
@RestController
@RequestMapping(value = "/auth/provider")
public class AuthProviderController {
    private Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * 通过userId获取用户信息
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/getUserInfo")
    @ResponseBody
    public RWrapper<JSONObject> getUserInfo(@RequestBody JSONObject jsonObject) {
        log.info("getUserInfo方法入参{}", jsonObject);
        JSONObject responseData = authService.getUserInfo(jsonObject);
        log.info("getUserInfo方法出参{}", responseData);
        return WrapperUtil.ok(responseData.getJSONObject("data"));
    }

}
