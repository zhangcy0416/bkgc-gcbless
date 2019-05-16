package com.bkgc.bless.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.BlessRequestParams;
import com.bkgc.bless.service.impl.BlessEnvelopeService;
import com.bkgc.common.utils.RequestParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.text.ParseException;


@Controller
@RequestMapping(value = "/blessenvelope")
public class BlessEnvelopeController extends BaseController {

    private Logger log = LoggerFactory.getLogger(BlessEnvelopeController.class);

    @Autowired
    private BlessEnvelopeService blessEnvelopeService;




    /**
     * 获取每个人的福包记录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getMyReceivedBlessEnvelopeList")
    @ResponseBody
    public JSONObject getMyReceivedBlessEnvelopeList(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getMyReceivedBlessEnvelopeList方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getMyReceivedBlessEnvelopeList(requestParam);
        log.info("getMyReceivedBlessEnvelopeList方法出参{}", responseData);
        return responseData;
    }

    /**
     * 获取20个自助机的福包数量
     *
     * @param requestParam
     * @param request
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @RequestMapping(value = "getDefault20MachinesByCurrentLoaction")
    @ResponseBody
    public JSONObject getDefault20MachinesByCurrentLoaction(BlessRequestParams requestParam, HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException {
        JSONObject requestData = RequestParamUtil.HandleRequestParam(requestParam);
        log.info("获取20个自助机的福包数量入参{}", requestData);
        JSONObject responseData = blessEnvelopeService.getDefault20MachinesByCurrentLoaction(requestData);
        log.info("获取20个自助机的福包数量出参{}", responseData);
        return responseData;
    }

    @RequestMapping(value = "/getReceivedBlessEnvelopeList")
    @ResponseBody
    public JSONObject getReceivedBlessEnvelopeList(BlessRequestParams requestParam, HttpServletRequest request) throws IllegalAccessException {
        return blessEnvelopeService.getReceivedBlessEnvelopeList(RequestParamUtil.HandleRequestParam(requestParam));
    }

    /**
     * 根据查询条件返回福包集合
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getBlessEnvelopeList")
    @ResponseBody
    public JSONObject getBlessEnvelopeList(HttpServletRequest request) throws ParseException {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getBlessEnvelopeList方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getBlessEnvelopeList(requestParam);
        log.info("getBlessEnvelopeList方法出参{}", responseData);
        return responseData;

    }


    /**
     * 根据自助机获取福包组信息
     *
     * @param request
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/getBlessEnvelopeListByMachineId")
    @ResponseBody
    public JSONObject getBlessEnvelopeListByMachineId(HttpServletRequest request) throws ParseException {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getBlessEnvelopeListByMachineId方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getBlessEnvelopeList(requestParam);
        log.info("getBlessEnvelopeListByMachineId方法出参{}", responseData);
        return responseData;
    }


    /**
     * 根据指定条件获取福包组集合信息
     *
     * @param request
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/getBlessEnvelopeGroupList")
    @ResponseBody
    public JSONObject getBlessEnvelopeGroupList(HttpServletRequest request) throws ParseException {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getBlessEnvelopeGroupList方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getBlessEnvelopeGroupList(requestParam);
        log.info("getBlessEnvelopeGroupList方法出参{}", responseData);
        return responseData;
    }


    /**
     * 根据自助机编码获取对应福包组信息
     *
     * @param request
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/getBlessEnvelopeGroupListByMachineId")
    @ResponseBody
    public JSONObject getBlessEnvelopeGroupListByMachineId(HttpServletRequest request) throws ParseException {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getBlessEnvelopeGroupListByMachineId方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getBlessEnvelopeGroupList(requestParam);
        log.info("getBlessEnvelopeGroupListByMachineId方法出参{}", responseData);
        return responseData;
    }


    @RequestMapping(value = "/getBlessEnvelopeGroupId")
    @ResponseBody
    public JSONObject getBlessEnvelopeGroupId(BlessRequestParams requestParam, HttpServletRequest request) throws IllegalAccessException {
        JSONObject requestData = RequestParamUtil.HandleRequestParam(requestParam);
        log.info("getBlessEnvelopeGroupId方法入参{}", requestData);
        JSONObject responseData = blessEnvelopeService.getBlessEnvelopeGroupId(requestData);
        log.info("getBlessEnvelopeGroupId方法出参{}", responseData);
        return responseData;

    }



    /**
     * 获取终端机发放福报的数据
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getGroupByMachineId")
    @ResponseBody
    public JSONObject getGroupByMachineId(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getGroupByMachineId方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getGroupByMachineId(requestParam);
        log.info("getGroupByMachineId方法出参{}", responseData);
        return responseData;

    }


    /**
     * 获取个人发放福报数据
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getGroupByUserId")
    @ResponseBody
    public JSONObject getGroupByUserId(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getGroupByUserId方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getGroupByUserId(requestParam);
        log.info("getGroupByUserId方法出参{}", responseData);
        return responseData;
    }

    /**
     * 通过自助机编码查询其所拥有的广告图片组
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getAdurlByMachineId")
    @ResponseBody
    public JSONObject getAdurlByMachineId(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getAdurlByMachineId方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getAdurlByMachineId(requestParam);
        log.info("getAdurlByMachineId方法出参{}", responseData);
        return responseData;

    }

    /**
     * 根据自助机获取每日福包领取数量
     *
     * @param request
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/getDailyStatisticByMachine")
    @ResponseBody
    public JSONObject getDailyStatisticByMachine(HttpServletRequest request) throws ParseException {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getDailyStatisticByMachine方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getDailyStatisticByMachine(requestParam);
        log.info("getDailyStatisticByMachine方法出参{}", responseData);
        return responseData;
    }


    /**
     * 指定时间内用户个人发福包组的个人和金额
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getGroupSumAndCountByUserId")
    @ResponseBody
    public JSONObject getGroupSumAndCountByUserId(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getGroupSumAndCountByUserId方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getGroupSumAndCountByUserId(requestParam);
        log.info("getGroupSumAndCountByUserId方法出参{}", responseData);
        return responseData;

    }

    /**
     * 根据商户guid 获取用户所创建福报计划的发放 的 总个数 总金额
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getGroupSumAndCountByChannelId")
    @ResponseBody
    public JSONObject getGroupSumAndCountByChannelId(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getGroupSumAndCountByChannelId方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getGroupSumAndCountByChannelId(requestParam);
        log.info("getGroupSumAndCountByChannelId方法出参{}", responseData);
        return responseData;
    }

    /**
     * 根据商户guid 获取用户所有机器发放的 已领取的福包 总个数 总金额
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getBlessSumAndCountByChannelId")
    @ResponseBody
    public JSONObject getBlessSumAndCountByChannelId(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getBlessSumAndCountByChannelId方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getBlessSumAndCountByChannelId(requestParam);
        log.info("getBlessSumAndCountByChannelId方法出参{}", responseData);
        return responseData;
    }


    @RequestMapping(value = "BlessReciverTop")
    @ResponseBody
    public JSONObject BlessReciverTop(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("BlessReciverTop方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getBlessReciverTop(requestParam);
        log.info("BlessReciverTop方法出参{}", responseData);
        return responseData;
    }

    @RequestMapping(value = "GetBlessGroupByChannelId")
    @ResponseBody
    public JSONObject GetBlessGroupByChannelId(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("GetBlessGroupByChannelId方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.getGroups(requestParam);
        log.info("GetBlessGroupByChannelId方法出参{}", responseData);
        return responseData;
    }


    @RequestMapping(value = "GetGroupByMcId")
    @ResponseBody
    public JSONObject GetGroupByMcId(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("GetGroupByMcId方法入参{}", requestParam);
        JSONObject responseData = blessEnvelopeService.GetGroupByMcId(requestParam);
        log.info("GetGroupByMcId方法出参{}", responseData);
        return responseData;
    }


//	/**
//	 * 小程序：福包统计
//	 * @param request
//	 * @return
//	 */
//	@RequestMapping(value="/GetGroupByMachineId")
//	@ResponseBody
//	public java.lang.String GetGroupByMachineId(HttpServletRequest request){
//
//			JSONObject requestParam = RequestParamUtil.getJsonParams(request);
//
//			return blessEnvelopeService.GetBlessInfo(requestParam);
//
//
//	}

}
