package com.bkgc.bless.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.service.impl.FeedbackService;
import com.bkgc.common.utils.RequestParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/feedback")
public class FeedBackController {

    private Logger log = LoggerFactory.getLogger(FeedBackController.class);

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 用户反馈
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/insertFeedback")
    @ResponseBody
    public JSONObject insertFeedback(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("用户反馈方法入参{}", requestParam);
        JSONObject responseData = feedbackService.insertFeedback(requestParam);
        log.info("用户反馈方法出参{}", responseData);
        return responseData;
    }
}
