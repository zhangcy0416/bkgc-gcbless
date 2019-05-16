package com.bkgc.bless.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.user.AuthMember;
import com.bkgc.bless.constant.CommonConfig;
import com.bkgc.bless.mapper.AuthMemberMapper;
import com.bkgc.bless.service.impl.MessageConsumer;
import com.bkgc.common.utils.RequestParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


@Controller
public class MessageController {

    private static Logger log = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageConsumer messageConsumer;

    @Autowired
    private AuthMemberMapper authMemberMapper;


    @RequestMapping(value = "/message/sendSMS")
    @ResponseBody
    public String sendSMS(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("jsonObject={}", jsonObject);
        String phone = request.getParameter("phone");
        String flag = request.getParameter("flag");
        //判断是哪个平台登录  31：福包天下安卓端 32：福包天下苹果端
        String clientID = jsonObject.getString("clientID");
        log.info("发送验证码的手机号{}，flag{}", phone, flag);
        if ("1".equals(flag)) {
            AuthMember authMember;
            if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                authMember = authMemberMapper.getAuthMemberByPhoneAndRole(phone, Integer.parseInt(CommonConfig.FBTX_ROLE_ID));
            } else {
                authMember = authMemberMapper.getNewAuthMemberByPhone(phone);
            }
            if (authMember != null) {
                JSONObject data = new JSONObject();
                data.put("code", "2042");
                data.put("msg", "该手机号已绑定");
                return data.toJSONString();
            }
        }
        String requestData;
        if (null != clientID && (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID))) {
            //fbtx代表是福包天下项目
            requestData = messageConsumer.sendMsgFbtx(phone, "fbtx");
        } else {
            requestData = messageConsumer.sendMsg(phone);
        }
        log.info("发送验证码返回数据{}", requestData);
        return requestData;
    }

    @RequestMapping(value = "/message/sendVoiceMsg")
    @ResponseBody
    public String sendVoiceMsg(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        String phone = request.getParameter("phone");
        String flag = request.getParameter("flag");
        //判断是哪个平台登录  31：福包天下安卓端 32：福包天下苹果端
        String clientID = jsonObject.getString("clientID");
        log.info("发送语音验证码的手机号{}，flag{}", phone, flag);
        if ("1".equals(flag)) {
            AuthMember authMember;
            if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                authMember = authMemberMapper.getAuthMemberByPhoneAndRole(phone, Integer.parseInt(CommonConfig.FBTX_ROLE_ID));
            } else {
                authMember = authMemberMapper.getNewAuthMemberByPhone(phone);
            }
            if (authMember != null) {
                JSONObject data = new JSONObject();
                data.put("code", "2042");
                data.put("msg", "该手机号已绑定");
                return data.toJSONString();
            }
        }
        String requestData = messageConsumer.sendVoiceMsg(phone);
        log.info("发送语音验证码返回数据{}", requestData);
        return requestData;
    }


    @RequestMapping(value = "/message/verify")
    @ResponseBody
    public String verify(HttpServletRequest request) {
        String phone = request.getParameter("phone");
        String code = request.getParameter("code");
        log.info("验证验证码的手机号{}，code{}", phone, code);
        String requestData = messageConsumer.verify(phone, code).toString();
        log.info("验证验证码返回数据{}", requestData);
        return requestData;
    }

}
