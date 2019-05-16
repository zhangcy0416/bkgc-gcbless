package com.bkgc.bless.web.provider;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.bless.BlessEnvelopeGroup;
import com.bkgc.bean.order.OrderSerachParam;
import com.bkgc.bless.service.impl.BlessEnvelopeService;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.WrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;

@RestController
public class BlessEnvController {

    @Autowired
    private BlessEnvelopeService blessEnvelopeService;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 福包条件查询
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/bless/getGroupStatistics")
    public RWrapper getGroupStatistics(@RequestBody OrderSerachParam param) {
        log.info("进入福包条件查询接口，参数={}", param.toString());
        return blessEnvelopeService.getGroupStatistics(param);
    }

    /**
     * 福包饼图
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/bless/getStatisticsByUser")
    public RWrapper getStatisticsByUser(@RequestBody List<Integer> list) {
        log.info("福包饼图入参={}", list);
        return blessEnvelopeService.getStatisticsByMachineIds(list);
    }


    /**
     * 3.1.1 福包列表
     *
     * @param
     * @return
     * @throws ParseException
     * @throws Exception
     */
    @RequestMapping(value = "/bless/GetBlessGroup")
    @ResponseBody
    public String GetBlessGroup(@RequestBody JSONObject params) throws ParseException {
        log.info("bless/GetBlessGroup方法入参={}", params);
        String responseData = blessEnvelopeService.GetBlessGroup(params);
        log.info("bless/GetBlessGroup方法出参={}", params);
        return responseData;
    }


    /**
     * 3.1.2 福包详情
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/bless/GetBlessInfo")
    @ResponseBody
    public String GetBlessInfo(@RequestBody JSONObject params) {
        log.info("bless/GetBlessInfo方法入参={}", params);
        String responseData = blessEnvelopeService.GetBlessInfo(params);
        log.info("bless/GetBlessInfo方法出参={}", params);
        return responseData;


    }

    /**
     * 3.1.3 领取记录
     *
     * @param
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/bless/GetBlessList")
    @ResponseBody
    public String GetBlessList(@RequestBody JSONObject params) throws ParseException {
        log.info("bless/GetBlessList方法入参={}", params);
        String responseData = blessEnvelopeService.GetBlessList(params);
        log.info("bless/GetBlessList方法出参={}", params);
        return responseData;
    }


    /**
     * 3.2.1	过期福包
     *
     * @param
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/bless/ExpiredBlessGroup")
    @ResponseBody
    public String ExpiredBlessGroup(@RequestBody JSONObject params) throws ParseException {
        log.info("bless/ExpiredBlessGroup方法入参={}", params);
        String responseData = blessEnvelopeService.ExpiredBlessGroup(params);
        log.info("bless/ExpiredBlessGroup方法出参={}", params);
        return responseData;
    }

    /**
     * 3.2.2 过期福包详情
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/bless/GetExpireBlessInfo")
    @ResponseBody
    public String GetExpireBlessInfo(@RequestBody JSONObject params) {
        log.info("bless/GetExpireBlessInfo方法入参={}", params);
        String responseData = blessEnvelopeService.GetBlessInfo(params);
        log.info("bless/GetExpireBlessInfo方法出参={}", params);
        return responseData;
    }

    /**
     * 3.2.3	广告福包
     *
     * @param
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/bless/getAdBlessGroup")
    @ResponseBody
    public String getAdBlessGroup(@RequestBody JSONObject params) throws ParseException {
        log.info("bless/getAdBlessGroup方法入参={}", params);
        String responseData = blessEnvelopeService.getAdBlessGroup(params);
        log.info("bless/getAdBlessGroup方法出参={}", params);
        return responseData;
    }

    /**
     * 3.2.4 广告福包详情
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/bless/GetAdBlessInfo")
    @ResponseBody
    public String GetAdBlessInfo(@RequestBody JSONObject params) {
        log.info("bless/GetAdBlessInfo方法入参={}", params);
        String responseData = blessEnvelopeService.GetBlessInfo(params);
        log.info("bless/GetAdBlessInfo方法出参={}", params);
        return responseData;


    }

    /**
     * 3.2.5	过期广告福包
     *
     * @param
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/bless/ExpiredAdBlessGroup")
    @ResponseBody
    public String ExpiredAdBlessGroup(@RequestBody JSONObject params) throws ParseException {
        log.info("bless/ExpiredAdBlessGroup方法入参={}", params);
        String responseData = blessEnvelopeService.ExpiredAdBlessGroup(params);
        log.info("bless/ExpiredAdBlessGroup方法出参={}", params);
        return responseData;
    }

    /**
     * 3.2.6  过期广告福包详情
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/bless/GetExpireAdBlessInfo")
    @ResponseBody
    public String GetExpireAdBlessInfo(@RequestBody JSONObject params) {
        log.info("bless/GetExpireAdBlessInfo方法入参={}", params);
        String responseData = blessEnvelopeService.GetBlessInfo(params);
        log.info("bless/GetExpireAdBlessInfo方法出参={}", params);
        return responseData;
    }


    @RequestMapping(value = "/bless/top10")
    @ResponseBody
    public RWrapper<List<BlessEnvelopeGroup>> top10(HttpServletRequest request) {
        List<BlessEnvelopeGroup> list = blessEnvelopeService.getTop10();
        return WrapperUtil.ok(list);
    }

}
