package com.bkgc.bless.service.impl;

import com.bkgc.bless.consumer.MessageFeignService;
import com.bkgc.common.utils.RWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class MessageConsumer {

    @Autowired
    private MessageFeignService messageFeignService;

    public String sendMsg(String phone) {
        Map<String, String> params = new HashMap<>();
        params.put("phone", phone);
        //RWrapper postForObject = restTemplate.postForObject("http://MESSAGE/sendSMS", params, RWrapper.class);
        RWrapper postForObject = messageFeignService.sendMsg(params);
        return postForObject.toString();
    }

    public String sendVoiceMsg(String phone) {
        Map<String, String> params = new HashMap<>();
        params.put("phone", phone);
        //RWrapper postForObject = restTemplate.postForObject("http://MESSAGE/sendVoiceMsg", params, RWrapper.class);
        RWrapper postForObject = messageFeignService.sendVoiceMsg(params);
        return postForObject.toString();
    }

    public RWrapper verify(String phone, String code) {
        Map<String, String> params = new HashMap<>();
        params.put("phone", phone);
        params.put("code", code);
        //RWrapper postForObject = restTemplate.postForObject("http://MESSAGE/verify", params, RWrapper.class);
        RWrapper postForObject = messageFeignService.verify(params);
        return postForObject;
    }

    public String sendMsgFbtx(String phone, String flag) {
        Map<String, String> params = new HashMap<>();
        params.put("phone", phone);
        params.put("flag", flag);
        RWrapper postForObject = messageFeignService.sendMsg(params);
        return postForObject.toString();
    }
    

}
