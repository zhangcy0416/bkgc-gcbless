package com.bkgc.game.web.Controller;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.constant.CommonConfig;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RequestParamUtil;
import com.bkgc.common.utils.StringUtil;
import com.bkgc.game.service.WxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/wx")
@Slf4j
public class WxController {

    @Autowired
    private WxService wxService;

    /**
     * App微信登录  微信登录每次都会请求这个地址
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/loginByApp")
    public String loginByApp(HttpServletRequest request) {
        String unionId = request.getParameter("unionId");
        String openId = request.getParameter("openId");
        String clientID = request.getParameter("clientID");
        log.info("开始进行微信登录 app微信登录请求unionId={},openId={}", unionId, openId);
        if (StringUtil.isNullOrEmpty(unionId, openId)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "微信unionId,openId为必填!");
        }
        log.info("clientID={}", clientID);
        if (StringUtil.isNullOrEmpty(clientID)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "clientID为必填!");
        }
        JSONObject jsonParam = RequestParamUtil.getJsonParams(request);
        log.info("请求的全部参数jsonParam={}", jsonParam);
        String retJson = wxService.loginByApp(jsonParam);
        log.info("app微信登录请求返回={}", retJson);
        return retJson;
    }


    /**
     * H5页面微信登录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/login")
    public String wxLogin(HttpServletRequest request) {
        String code = request.getParameter("code");
        log.info("进入方法名称：wxLogin   微信登录请求code=" + code);
        if (StringUtil.isNullOrEmpty(code)) {
            JSONObject data = new JSONObject();
            data.put("code", 2000);
            data.put("msg", "微信code为必填");
            return data.toString();
        }

        JSONObject retJson = wxService.wxLogin(code);
        log.info("微信登录请求返回=" + retJson);

        return retJson.toString();
    }

    /**
     * 福包天下用户获取微信个人信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/newLogin")
    public String newLogin(HttpServletRequest request) {
        String code = request.getParameter("code");
        log.info("福包天下用户获取微信个人信请求code={}", code);
        if (StringUtil.isNullOrEmpty(code)) {
            throw new BusinessException(ResultCodeEnum.FAIL, "微信code为不可以为空");
        }
        JSONObject retJson = wxService.wxNewLogin(code);
        log.info("微信登录请求返回={}", retJson);

        return retJson.toString();
    }


    /**
     * H5页面微信注册
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/register")
    public String register(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("进入方法名称：register  微信注册开始入参={}", jsonObject);
        String phone = request.getParameter("phone");
        String smgCode = request.getParameter("smgCode");
        String unionId = request.getParameter("unionId");
        //phone = null;
        if (StringUtil.isNullOrEmpty(phone, smgCode, unionId)) {
            JSONObject data = new JSONObject();
            data.put("code", 2000);
            data.put("msg", "请检查参数 [phone ,smgCode,unionId]");
            return data.toString();
        }
        String sex = request.getParameter("sex");
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);

        switch (sex) {
            case "1":
                requestParam.put("sex", "男");
                break;
            case "2":
                requestParam.put("sex", "女");
                break;
            default:
                requestParam.put("sex", "未知");
                break;
        }
        return wxService.register(requestParam);
    }

    /**
     * fbtx微信注册
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/newRegister")
    public String newRregister(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("fbtx微信注册入参={}", jsonObject);
        String phone = request.getParameter("phone");
        String smgCode = request.getParameter("smgCode");
        String unionId = request.getParameter("unionId");
        //phone = null;
        if (StringUtil.isNullOrEmpty(phone, smgCode, unionId)) {
            JSONObject data = new JSONObject();
            data.put("code", 2000);
            data.put("msg", "请检查参数 [phone ,smgCode,unionId]");
            return data.toString();
        }
        String sex = request.getParameter("sex");
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);

        switch (sex) {
            case "1":
                requestParam.put("sex", "男");
                break;
            case "2":
                requestParam.put("sex", "女");
                break;
            default:
                requestParam.put("sex", "未知");
                break;
        }
        requestParam.put("clientID", CommonConfig.ANDROID_CLIENT_ID);
        String res = wxService.register(requestParam);
        log.info("fbtx微信注册入参返回数据={}", res);
        return res;
    }


    /**
     * 获取夺宝游戏top10 ，幸运值，福包金额
     *
     * @return
     */
    @RequestMapping(value = "/getInfo4Game")
    public String getInfo4Game(HttpServletRequest request) {
        String guid = request.getParameter("guid");
        log.info("getInfo4Game方法guid={}", guid);
        if (StringUtil.isNullOrEmpty(guid)) {
            JSONObject data = new JSONObject();
            data.put("code", 2020);
            data.put("msg", "guid 为必须参数");
            return data.toString();
        }
        String requestData = wxService.getInfo4Game(guid);
        log.info("getInfo4Game方法返回数据{}", requestData);
        return requestData;
    }
}
