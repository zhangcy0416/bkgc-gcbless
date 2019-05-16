package com.bkgc.bless.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.consumer.PaymentFeignService;
import com.bkgc.bless.service.impl.AuthService;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RequestParamUtil;
import com.bkgc.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Controller
@RequestMapping(value = "/auth")
//@Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
public class AuthController extends BaseController {

    private Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private PaymentFeignService paymentFeignService;


/*    *//**
     * 普通方式登录
     *
     * @param request
     * @return
     *//*
    @RequestMapping(value = "/login")
    @ResponseBody
    public JSONObject login(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("login方法入参{}", requestParam);
        JSONObject responseData = authService.login(requestParam);
        log.info("login方法出参{}", responseData);
        return responseData;
    }*/

    /**
     * 第三方微信登录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/loginByWeiXin")
    @ResponseBody
    public JSONObject loginByWeiXin(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("loginByWeiXin方法入参{}", requestParam);
        JSONObject responseData = authService.loginByWeiXin(requestParam);
        log.info("loginByWeiXin方法出参{}", responseData);
        return responseData;
    }


    /**
     * 更新用户基本信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/updateUserInfo")
    @ResponseBody
    public JSONObject updateInfo(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("updateUserInfo方法入参{}", requestParam);
        JSONObject responseData = authService.updateUserInfo(requestParam, request);
        log.info("updateUserInfo方法出参{}", responseData);
        return responseData;

    }

    /**
     * 通过userId获取用户信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getUserInfo")
    @ResponseBody
    public JSONObject getUserInfo(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("getUserInfo方法入参{}", requestParam);
        JSONObject responseData = authService.getUserInfo(requestParam);
        log.info("getUserInfo方法出参{}", responseData);
        return responseData;
    }

    /**
     * 通过手机号注册用户
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/registerWithPhone")
    @ResponseBody
    public JSONObject bindPhone(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("registerWithPhone方法入参{}", requestParam);
        JSONObject responseData = authService.registerWithPhone(requestParam);
        log.info("registerWithPhone方法出参{}", responseData);
        return responseData;
    }


    /**
     * 检查手机号是否已注册，如已注册 返回已注册信息，否则进行注册，并返回注册信息（不需要token）
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/checkSmsCodeGetUser")
    @ResponseBody
    public JSONObject checkUser(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("checkSmsCodeGetUser方法入参{}", requestParam);
        JSONObject responseData = authService.checkUser(requestParam);
        log.info("checkSmsCodeGetUser方法出参{}", responseData);
        return responseData;
    }


    /**
     * 用户修改登录密码
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/updatePassword")
    @ResponseBody
    public JSONObject updatePassword(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("updatePassword方法入参{}", requestParam);
        JSONObject responseData = authService.updatePassword(requestParam);
        log.info("updatePassword方法出参{}", responseData);
        return responseData;
    }

    /**
     * 用户修改绑定手机号
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/updatePhone")
    @ResponseBody
    public JSONObject updatePhone(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("updatePhone方法入参{}", requestParam);
        JSONObject responseData = authService.updatePhone(requestParam);
        log.info("updatePhone方法出参{}", responseData);
        return responseData;
    }

    /**
     * 获取头像地址
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    @RequestMapping(value = "/getImage")
    public void getImage(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        authService.getImage(request, response);
    }

    /**
     * 更新用户真实信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/updateRealInfo")
    @ResponseBody
    public JSONObject updateRealInfo(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("updateRealInfo方法入参{}", requestParam);
        JSONObject responseData = authService.updateRealInfo(requestParam);
        log.info("updateRealInfo方法出参{}", responseData);
        return responseData;
    }


    /**
     * 手机号验证码登录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/loginWithTerminalInfo")
    @ResponseBody
    public JSONObject loginWithTerminalInfo(HttpServletRequest request) {
        JSONObject requestParam = RequestParamUtil.getJsonParams(request);
        log.info("手机号验证码登录方法入参{}", requestParam);
        JSONObject responseData = authService.loginWithTerminalInfo(requestParam);
        log.info("手机号验证码登录方法出参{}", responseData);
        return responseData;
    }


    @RequestMapping(value = "/batchTransfer")
    public int batchTransfer() {
        //批量转移数据
        return authService.batchTransfer();
        //authService.createRandomNumber();
        //authService.registWithOutCode();
    }

    /**
     * 验证支付密码是否正确
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/checkPaymentPassword", method = RequestMethod.POST)
    public String checkPaymentPassword(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("验证支付密码是否正确入参={}", jsonObject);
        String userId = jsonObject.getString("userId");
        String password = jsonObject.getString("password");
        if (StringUtil.isNullOrEmpty(userId, password)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "必传参数为空");
        }
        return authService.checkPaymentPassword(jsonObject);
    }


    /**
     * <p>Title:      查询指纹支付的状态 </p>
     * <p>Description  </p>
     *
     * @Author        zhangft
     * @CreateDate    2018/9/7 下午1:59
     */
    @ResponseBody
    @RequestMapping(value = "/queryFingerPayStatus", method = RequestMethod.POST)
    public String queryFingerPayStatus(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("查询是否设置指纹支付={}", jsonObject.toString());

        return authService.queryFingerPayStatus(jsonObject);

    }

    /**
     * <p>Title:      设置指纹支付密码 </p>
     * <p>Description  </p>
     *
     * @Author        zhangft
     * @CreateDate    2018/9/7 下午2:00
     */
    @ResponseBody
    @RequestMapping(value = "/setFingerPayPwd", method = RequestMethod.POST)
    public String setFingerPayPwd(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("查询是否设置指纹支付={}", jsonObject.toString());

        return authService.setFingerPayPwd(jsonObject);

    }

    /**
     * <p>Title:      检查设备指纹支付是否正确 </p>
     * <p>Description  </p>
     *
     * @Author        zhangft
     * @CreateDate    2018/9/7 下午2:00
     */
    @ResponseBody
    @RequestMapping(value = "/checkFingerPayPwd", method = RequestMethod.POST)
    public String checkFingerPayPwd(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("查询是否设置指纹支付={}", jsonObject.toString());

        return authService.checkFingerPayPwd(jsonObject);

    }

}
