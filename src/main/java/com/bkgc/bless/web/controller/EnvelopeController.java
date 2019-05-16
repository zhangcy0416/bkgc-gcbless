package com.bkgc.bless.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.service.impl.EnvelopeService;
import com.bkgc.bless.service.impl.MysqlLockService;
import com.bkgc.common.utils.RequestParamUtil;
import com.bkgc.common.utils.WrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title:      EnvelopeController </p>
 * <p>Description  </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author zhangft
 * @CreateDate 2017/12/21 上午10:34
 */
@Controller
@RequestMapping(value = "/envelope")
public class EnvelopeController {

    private static Logger log = LoggerFactory.getLogger(EnvelopeController.class);

    @Autowired
    private EnvelopeService envelopeService;

    @Autowired
    private MysqlLockService mysqlLockService;


    /**
     * 用户领福包
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getBlessEnvelope")
    @ResponseBody
    public String getBlessEnvelope(HttpServletRequest request) {
        JSONObject requestParams = RequestParamUtil.getJsonParams(request);
        log.info("用户领福包参数=" + requestParams.toString());

        JSONObject retJson ;
        try {

            mysqlLockService.setLockID("getEnvelope");
            mysqlLockService.setLockTimeSecond(10);

            //加锁
            mysqlLockService.lock();
            retJson = envelopeService.getBlessEnvelope(requestParams);
        }finally {
            //解锁
            mysqlLockService.unlock();
        }

        log.info("用户领福包返回=" + requestParams.toString());
        return WrapperUtil.ok(retJson).toString();
    }

}
