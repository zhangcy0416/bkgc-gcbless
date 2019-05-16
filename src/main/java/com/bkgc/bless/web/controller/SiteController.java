package com.bkgc.bless.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.service.impl.SiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping(value = "/site")
public class SiteController extends BaseController {

    private Logger log = LoggerFactory.getLogger(SiteController.class);

    @Autowired
    private SiteService siteService;

    @RequestMapping(value = "/getPlayContentList")
    @ResponseBody
    public JSONObject getPlayContentList() {
        log.info("getPlayContentList方法");
        JSONObject responseData = siteService.getPlayContentList();
        log.info("getPlayContentList方法出参{}", responseData);
        return responseData;
    }


}
