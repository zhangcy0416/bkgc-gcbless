package com.bkgc.bless.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.machine.ApkVersion;
import com.bkgc.bless.config.Config;
import com.bkgc.bless.service.impl.ApkVersionService;
import com.bkgc.bless.service.impl.TestUserService;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RequestParamUtil;
import com.bkgc.common.utils.ResultUtil;
import com.bkgc.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/apk")
public class ApkVersionController {
    private Logger log = LoggerFactory.getLogger(ApkVersionController.class);

    @Autowired
    private ApkVersionService apkVersionService;

    @Autowired
    private TestUserService testUserService;

    @Resource
    private Config config;

    /**
     * 通过版本号和渠道查找最新版本
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/checkVersion")
    @ResponseBody
    public JSONObject checkVersion(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("checkVersion方法入参{}", requestParam);
        JSONObject responseData = apkVersionService.checkVersion(requestParam);
        log.info("checkVersion方法出参{}", responseData);
        return responseData;
    }


    /**
     * 查询ios最大版本
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/ios/hidden")
    @ResponseBody
    public JSONObject ios(HttpServletRequest request) {
        String maxVersion = apkVersionService.getMaxVersion();
        //log.info("ios最大版本号{}", maxVersion);
        return ResultUtil.buildSuccessResult(1000, maxVersion);
    }



    @RequestMapping(value = "/checkNewVersion")
    @ResponseBody
    public JSONObject checkNewVersion(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("checkNewVersion方法入参{}", requestParam);
        JSONObject responseData = apkVersionService.checkNewVersion(requestParam);
        log.info("checkNewVersion方法出参{}", responseData);
        return responseData;
    }



    @RequestMapping(value = "/ios/show")
    @ResponseBody
    public JSONObject isHidden(HttpServletRequest request) {
        String maxVersion = request.getParameter("maxVersion");
        log.info("进入查询最大版本是否显示，最大版本={}", maxVersion);
        ApkVersion apkVersion = apkVersionService.getMaxVersion(maxVersion);
        log.info("ios最大版本号={}", maxVersion);
        if (apkVersion == null) {
            return ResultUtil.buildSuccessResult(1000, 0);
        }
        return ResultUtil.buildSuccessResult(1000, apkVersion.getShow());
    }

    /**
     * <p>Title:      是否是内测用户 </p>
     * <p>Description  </p>
     *
     * @Author        zhangft
     * @CreateDate    2018/7/27 下午1:57
     */
    @RequestMapping(value = "/test/isTestUser")
    @ResponseBody
    public JSONObject isTestUser(HttpServletRequest request) {

        if (config.getIsTestUserOn() == 1) {  //开启内测用户查询
            String userId = request.getParameter("userId");

            log.info("查询是否是内测用户userId={}", userId);

            if(StringUtil.isNullOrEmpty(userId)){
                throw new BusinessException(ResultCodeEnum.PARAM_INVIDATE, "userId不能为空");
            }
            int c = testUserService.isTestUser(userId);

            log.info("查询结果={}", c);
            return ResultUtil.buildSuccessResult(1000, c);
        }else{
            return ResultUtil.buildSuccessResult(1000, 1);
        }



    }
}
