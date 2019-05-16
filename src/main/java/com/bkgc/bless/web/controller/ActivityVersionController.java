package com.bkgc.bless.web.controller;

import com.bkgc.bean.bless.ActivityVersion;
import com.bkgc.bless.service.impl.ActivityVersionService;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.WrapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/activity")
@Slf4j
public class ActivityVersionController {

    @Autowired
    private ActivityVersionService activityVersionService;

    @RequestMapping(value = "/getActivityInfo")
    @ResponseBody
    public RWrapper<ActivityVersion> getActivityInfo(HttpServletRequest request) {

        String activityId = request.getParameter("activityId");
        String version = request.getParameter("version");

        log.info("getActivityInfo方法入参activityId={},version={}", activityId, version);

        ActivityVersion activityVersionInfo = activityVersionService.getActivityVersionInfo(activityId, version);

        if (activityVersionInfo == null) {
            log.info("未查询到活动版本信息");
            return WrapperUtil.error(ResultCodeEnum.ERR_41092);
        }else{
            return WrapperUtil.ok(activityVersionInfo);
        }


    }

}
