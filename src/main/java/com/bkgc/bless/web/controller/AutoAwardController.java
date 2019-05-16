package com.bkgc.bless.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.award.LottoAutoawardOrder;
import com.bkgc.bless.consumer.AwardFeignService;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.DateTimeUtils;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.RequestParamUtil;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;

@Controller
@RequestMapping("/autoaward")
public class AutoAwardController {

    @Autowired
    private AwardFeignService awardFeignService;

    private Logger log = LoggerFactory.getLogger(this.getClass());


    /**
     * <p>Title:      自动兑奖信息上传 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2017/11/28 下午5:16
     */
    @RequestMapping(value = "/lottoCodeUpload", method = RequestMethod.POST)
    @ResponseBody
    public String lottoCodeUpload(HttpServletRequest request) {

        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);

        log.info("上传兑奖信息=" + jsonObject.toString());

        String retStr = awardFeignService.lottoCodeUpload4AutoAward(jsonObject).toString();

        log.info("上传兑奖信息返回=" + retStr);

        return retStr;

    }

    /**
     * <p>Title:      自动兑奖订单状态查询 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2017/11/28 下午5:16
     */
    @RequestMapping(value = "/autoAwardStatusQuery", method = RequestMethod.POST)
    @ResponseBody
    public String autoAwardStatusQuery(HttpServletRequest request) {

        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);

        log.info("查询自动兑奖订单信息参数=" + jsonObject.toString());

        JSONObject retJson = new JSONObject();

        RWrapper<LottoAutoawardOrder> lottoAutoawardOrderRWrapper = awardFeignService.autoAwardStatusQuery(jsonObject);
        if (ResultCodeEnum.SUCCESS.getCode().equals(lottoAutoawardOrderRWrapper.getCode())) {
            //查询成功，封装数据
            if (lottoAutoawardOrderRWrapper.getData() != null) {
                LottoAutoawardOrder lottoAutoawardOrder = lottoAutoawardOrderRWrapper.getData();
                //兑奖成功
                if (lottoAutoawardOrder.getAutoawardstatus() == 1 || lottoAutoawardOrder.getAwardstatus() == 1) {
                    retJson.put("code", ResultCodeEnum.SUCCESS.getCode());
                    retJson.put("msg", "兑奖处理成功");
                    JSONObject dataJson = new JSONObject();
                    dataJson.put("awardflag", lottoAutoawardOrder.getAwardflag());
                    dataJson.put("awardmoney", lottoAutoawardOrder.getAwardmoney());
                    dataJson.put("awardresulttype", lottoAutoawardOrder.getAwardresulttype());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dataJson.put("awardtime", sdf.format(lottoAutoawardOrder.getAwardtime()));
                    dataJson.put("orderNo", lottoAutoawardOrder.getOrderno());
                    dataJson.put("gameName", lottoAutoawardOrder.getInstantgamename());
                    dataJson.put("remark", lottoAutoawardOrder.getRemark());
                    dataJson.put("sell", lottoAutoawardOrder.getSell() == null ? "" : lottoAutoawardOrder.getSell());
                    dataJson.put("denomination", lottoAutoawardOrder.getDenomination() == null ? "" : lottoAutoawardOrder.getDenomination());
                    retJson.put("data", dataJson);
                } else if (lottoAutoawardOrder.getAutoawardstatus() == 2) {
                    retJson.put("code", ResultCodeEnum.ERR_46008.getCode());
                    retJson.put("msg", ResultCodeEnum.ERR_46008.getMsg());
                } else {
                    retJson.put("code", ResultCodeEnum.ERR_46007.getCode());
                    retJson.put("msg", ResultCodeEnum.ERR_46007.getMsg());
                }
            }
        } else {
            //查询失败，返回正在兑奖中,可以继续轮询
            retJson.put("code", ResultCodeEnum.ERR_46007.getCode());
            retJson.put("msg", ResultCodeEnum.ERR_46007.getMsg());
        }

        log.info("查询自动兑奖订单信息返回=" + retJson.toString());

        return retJson.toString();

    }

    /**
     * <p>Title:      兑奖记录查询 </p>
     * <p>Description  </p>
     *
     * @Author zhangft
     * @CreateDate 2017/11/29 下午4:01
     */
    @RequestMapping(value = "/awardRecordsQuery", method = RequestMethod.POST)
    @ResponseBody
    public String awardRecordsQuery(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("兑奖记录查询入参{}", jsonObject.toJSONString());
        JSONObject retJson = new JSONObject();
        JSONObject dataJson = new JSONObject();
        RWrapper<PageInfo<LottoAutoawardOrder>> pageInfoRWrapper = awardFeignService.awardRecordsQuery(jsonObject);
        log.info("调用兑奖系统返回数据{}", pageInfoRWrapper);
        JSONArray list = new JSONArray();
        for (LottoAutoawardOrder order : pageInfoRWrapper.getData().getList()) {
            int status;
            if ("1".equals(order.getAwardstatus() + "") || "1".equals(order.getAutoawardstatus() + "")) {
                status = 5;
            } else {
                status = 2;
            }
            String remark = order.getRemark();
            Integer amount = order.getAwardmoney();
            if (amount == null) {
                amount = 0;
            }
            Integer awardResultType = order.getAwardresulttype();
            JSONObject d = new JSONObject();
            d.put("Id", order.getId());
            d.put("TicketName", order.getTickettype());
            d.put("Amount", amount == 0 ? "--" : amount);
            d.put("Level", order.getAwardlevel());
            d.put("Status", status);
            if (status == 2) {
                d.put("Remark", "");
            } else {
                d.put("Remark", remark);
            }
            d.put("awardResultType", awardResultType);
            d.put("CreateTime", DateTimeUtils.getTimestamp(order.getRequesttime()));
            d.put("TicketNumber", order.getTicketno());
            list.add(d);
        }
        dataJson.put("List", list);
        dataJson.put("count", pageInfoRWrapper.getData().getTotal());

        retJson.put("code", pageInfoRWrapper.getCode());
        retJson.put("msg", pageInfoRWrapper.getMsg());
        retJson.put("data", dataJson);
        log.info("兑奖记录查询出参{}", retJson.toJSONString());
        return retJson.toString();

    }


}
