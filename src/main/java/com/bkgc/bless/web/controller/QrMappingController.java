package com.bkgc.bless.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.service.impl.EnvelopeService;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * <p>Title:      QrMappingController </p>
 * <p>Description  </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author zhangft
 * @CreateDate 2017/12/22 上午10:56
 */
@Controller
@RequestMapping("/q")
public class QrMappingController {

    private Logger log = LoggerFactory.getLogger(QrMappingController.class);

    @Autowired
    private EnvelopeService envelopeService;

    /**
     * <p>Title:      领福包二维码，解析随机码，跳转对应页面，支持福包app和微信以及第三方浏览器 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2017/12/22 上午10:58
     */
    @RequestMapping(value = "/m")
    @ResponseBody
    public void mappingURL(HttpServletRequest request, HttpServletResponse res) throws IOException {
        JSONObject requestParam = new JSONObject();
        Map<String, String[]> requestMap = request.getParameterMap();
        Set<String> keySet = requestMap.keySet();
        Object[] array = keySet.toArray();
        log.info("参数长度={}", array.length);
        if (array.length == 1) {
            requestParam.put("randNum", array[0]);
            requestParam.put("source", "1");  //默认微信扫自助机二维码
        } else if (array.length == 2) {
            if(array[0].toString().length()>array[1].toString().length()){

                requestParam.put("randNum", array[1]);
                requestParam.put("userId", array[0]);
            }else{

                requestParam.put("randNum", array[0]);
                requestParam.put("userId", array[1]);
            }

            requestParam.put("source", "2");  //国彩app
        } else {
            throw new BusinessException(ResultCodeEnum.PARAM_INVIDATE, "参数异常");
        }
        log.info("进入扫描二维码接口，参数为：" + requestParam.toString());
        envelopeService.mappingURL(requestParam, res);
    }
}
