package com.bkgc.bless.consumer;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.common.utils.RWrapper;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * Created by zhouyuzhao on 2018/4/17.
 */
@FeignClient("message")
public interface MessageFeignService {

    /**
     * 发送短信验证码
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/sendSMS", method = RequestMethod.POST)
    RWrapper sendMsg(@RequestBody Map<String, String> map);

    /**
     * 发送语音验证码
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/sendVoiceMsg", method = RequestMethod.POST)
    RWrapper sendVoiceMsg(@RequestBody Map<String, String> map);

    /**
     * 验证短信验证码
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    RWrapper verify(@RequestBody Map<String, String> map);


    @RequestMapping(value = "/sendByAlias", method = RequestMethod.POST)
    RWrapper<JSONObject> sendByAlias(@RequestBody JSONObject jsonObject);

    /**
     * 判断个人是否有未读消息
     *
     * @return
     */
    @RequestMapping("/queryIsNotReadMsg")
    RWrapper queryIsNotReadMsg(@RequestBody JSONObject jsonObject);

    /**
     * 查询所有的个人消息
     *
     * @return
     */
    @RequestMapping(value = "/getAllMsg")
    RWrapper getAllMsg(@RequestBody JSONObject jsonObject);

    /**
     * 将未读的个人消息标记为已读
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/updateMsgByAlias")
    RWrapper updateMsgByAlias(@RequestBody JSONObject jsonObject);

    /**
     * 根据id查询所有的消息标志
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/queryAllFlagByMsgIds")
    RWrapper queryAllFlagByMsgIds(@RequestBody JSONObject jsonObject);

    /**
     * 查询所有公共消息接口
     *
     * @return
     */
    @RequestMapping(value = "/getAllCommonMsg")
    public RWrapper getAllCommonMsg();


    /**
     * 通过用户的id去获取融云的token
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/rc/getToken", method = RequestMethod.POST)
    RWrapper<JSONObject> getToken(@RequestBody JSONObject jsonObject);


    /**
     * 根据userID获取用户详情
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/rc/getUserInfoByUserId", method = RequestMethod.POST)
    RWrapper<JSONObject> getUserInfoByUserId(@RequestBody JSONObject jsonObject);

    /**
     * 变为空间主
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/rc/updateOwner", method = RequestMethod.POST)
    RWrapper updateOwner(@RequestBody JSONObject jsonObject);

    /**
     * 更改头像
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/rc/updateFace", method = RequestMethod.POST)
    RWrapper updateFace(@RequestBody JSONObject jsonObject);
}
