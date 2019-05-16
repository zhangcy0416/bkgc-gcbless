package com.bkgc.game.web.Controller;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.consumer.MessageFeignService;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.RequestParamUtil;
import com.bkgc.common.utils.StringUtil;
import com.bkgc.game.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by gmg on on 2017-12-21 15:59.
 */
@RestController
@RequestMapping("/game")
public class GameController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    GameService gameService;

    @Autowired
    private MessageFeignService messageFeignService;

    /**
     * 开始游戏, 此方法包括以下功能: 调用福包支付,当支付成功后加载游戏规则,
     * 游戏规则会返回一个确定的奖品id, 获取到此id后连同guid一起返回给游戏页面
     *
     * @param guid 用户id
     * @return
     * @throws RuntimeException
     */
    @RequestMapping(value = "/startGame")
    public String startGame(String guid) {
        log.info("startGame参数guid为={}", guid);

        if(StringUtil.isNullOrEmpty(guid)){
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "guid参数不能为空");
        }

        String responseData = gameService.gameStart(guid);
        log.info("startGame方法出参={}", guid);
        return responseData;
    }

    /**
     * 打折卡使用方法,具体流程参考文档
     *
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/useDiscountCard")
    public String useDiscountCard(HttpServletRequest request) {
        JSONObject param = RequestParamUtil.getJsonParams(request);
        log.info("使用打折卡接口参数为={}", param.toString());
        String responseData = gameService.useDiscountCard(param);
        log.info("使用打折卡接口出参为={}", responseData);
        return responseData;
    }


    /**
     * 添加实物配送信息
     *
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/useMailedAward")
    public String useMailedAward(HttpServletRequest request) {
        JSONObject param = RequestParamUtil.getJsonParams(request);
        log.info("使用实物寄送 接口参数为{}", param);
        String responseData = gameService.useMailedAward(param);
        log.info("使用实物寄送 返回参数为{}", responseData);
        return responseData;
    }

    /**
     * 使用奖金翻倍卡
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/useAwardCard", method = RequestMethod.POST)
    public String useAwardCard(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("使用奖金翻倍卡入参为={}", jsonObject);
        String responseData = gameService.useAwardCard(jsonObject);
        log.info("使用奖金翻倍卡出参为={}", responseData);
        return responseData;
    }


    /**
     * 根据用户获取对应卡包集合
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getReward", method = RequestMethod.POST)
    @ResponseBody
    public RWrapper getReward(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("参数为" + jsonObject.toString());
        RWrapper res = gameService.getReward(jsonObject);
        log.info("根据用户获取对应卡包集合，返回参数={}", res.toString());
        return res;
    }


    /**
     * 获取可用奖品
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getAliveReward", method = RequestMethod.POST)
    public String getAliveReward(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("参数为" + jsonObject.toString());
        String res = gameService.getAliveReward(jsonObject);
        log.info("获取可用奖品返回参数", res);
        return res;
    }


    /**
     * 获取用户收货地址
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getAddress", method = RequestMethod.POST)
    public RWrapper getAddress(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("参数为" + jsonObject.toString());
        RWrapper res = gameService.getAddress(jsonObject);
        log.info("getAddress返回参数为{}", res.toString());
        return res;
    }

    /**
     * 更改用户收货地址
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/updateAddress", method = RequestMethod.POST)
    public String updateAddress(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info(" 更改用户地址 参数为" + jsonObject.toString());
        String responseData = gameService.updateAddress(jsonObject);
        log.info(" 更改用户地址返回参数={}", jsonObject.toString());
        return responseData;
    }

    /**
     * 获取有效期内的未翻倍的订单列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getValidAwardList", method = RequestMethod.POST)
    public String getValidAwardList(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("获取有效期内的未翻倍的订单列表入参={}", jsonObject);
        String responseData = gameService.getValidAwardList(jsonObject);
        log.info("获取有效期内的未翻倍的订单列表出参={}", responseData);
        return responseData;
    }


    /**
     * 该方法是用于福包多宝界面获取奖品列表
     */
    @RequestMapping(value = "getAwardList")
    public RWrapper getAwardList(HttpServletRequest request) {
//        JSONObject params = RequestParamUtil.getJsonParams(request);
//        log.info("奖品列表接口请求参数:"+params);
        return gameService.getAwardList();
    }

    /**
     * 个人中心----夺宝记录
     *
     * @return
     */
    @RequestMapping(value = "/rewardRecord", method = RequestMethod.POST)
    public String rewardRecord(String guid, String pageNum, String pageSize) {
        log.info("开始个人的夺宝记录!guid={},pageNum={},pageSize={}", guid, pageNum, pageSize);
        if (StringUtil.isNullOrEmpty(guid, pageNum, pageSize)) {
            throw new BusinessException(ResultCodeEnum.EXTEND_ERR, "必填参数为空！");
        }
        pageNum = String.valueOf(Integer.parseInt(pageNum) + 1);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("guid", guid);
        jsonObject.put("pageNum", pageNum);
        jsonObject.put("pageSize", pageSize);
        String resData = gameService.rewardRecord(jsonObject);
        log.info("返回给前端的夺宝记录resData={}", resData);
        return resData;
    }

    /**
     * 更新配送信息表
     *
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateUseMailedAward")
    public String updateUseMailedAward(HttpServletRequest request) {
        JSONObject param = RequestParamUtil.getJsonParams(request);
        log.info("使用实物寄送 接口参数为{}", param);
        String responseData = gameService.updateUseMailedAward(param);
        log.info("使用实物寄送 返回参数为{}", responseData);
        return responseData;
    }

    /**
     * 查看实物配送信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getUseMailedAward", method = RequestMethod.POST)
    public RWrapper getUseMailedAward(HttpServletRequest request) {
        JSONObject jsonObject = RequestParamUtil.getJsonParams(request);
        log.info("查看实物配送信息的参数={}" + jsonObject.toString());
        RWrapper responseData = gameService.getUseMailedAward(jsonObject);
        log.info("查看实物配送信息的返回参数={}", responseData.toString());
        return responseData;
    }

    /**
     * 判断个人是否有未读消息
     *
     * @param userId
     * @return
     */
    @RequestMapping("/queryIsNotReadMsg")
    public RWrapper queryIsNotReadMsg(String userId) {

        log.info("判断个人是否有未读消息入参={}", userId);
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.EXTEND_ERR, "必填参数为空！");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        RWrapper responseData = messageFeignService.queryIsNotReadMsg(jsonObject);
        log.info("判断个人是否有未读消息，返回参数={}", responseData);
        return responseData;
    }

    /**
     * 查询个人消息列表
     *
     * @return
     */
    @RequestMapping(value = "/getAllMsg")
    public RWrapper getMailRewardList(Integer pageNum, Integer pageSize, String userId) {

        log.info("查询个人消息入参, pageNum={},pageSize={},userId={}", pageNum, pageSize, userId);
        if (pageNum == null || pageNum == null || StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "缺少必选参数[pageNum, pageSize, userId]");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pageNum", ++pageNum);
        jsonObject.put("pageSize", pageSize);
        jsonObject.put("userId", userId);
        RWrapper rWrapper = messageFeignService.getAllMsg(jsonObject);
        log.info("查询个人消息返回结果为{}", rWrapper);
        return rWrapper;
    }

    /**
     * 将未读的个人消息标记为已读
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/updateMsgByAlias", method = RequestMethod.POST)
    public String updateMsgByAlias(String userId) {

        log.info("将未读的个人消息标记为已读,消息入参={}", userId);
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.EXTEND_ERR, "必填参数为空！");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        RWrapper responseData = messageFeignService.updateMsgByAlias(jsonObject);
        log.info("将未读的个人消息标记为已读,返回参数={}", responseData);
        return responseData.toString();
    }

    /**
     * 查询消息的删除标记
     *
     * @param userIds 消息Id字符串
     * @return
     */
    @RequestMapping("/queryAllFlagByMsgIds")
    public RWrapper queryAllFlagByMsgIds(String userIds) {

        log.info("查询消息的删除标记,消息入参={}", userIds);
        if (StringUtil.isNullOrEmpty(userIds)) {
            throw new BusinessException(ResultCodeEnum.EXTEND_ERR, "必填参数为空！");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userIds", userIds);
        RWrapper responseData = messageFeignService.queryAllFlagByMsgIds(jsonObject);
        log.info("查询消息的删除标记,返回参数={}", responseData);
        return responseData;
    }


    /**
     * 获取卡
     *
     * @param guid
     * @param rewardType
     * @return
     */
    @RequestMapping(value = "/showMultipleCards", method = RequestMethod.POST)
    public String showMultipleCards(String guid, String rewardType) {
        log.info("获取卡入参guid={},rewardType={}", guid, rewardType);
        if (StringUtil.isNullOrEmpty(guid, rewardType)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "必填参数不能为空!");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("guid", guid);
        jsonObject.put("rewardType", rewardType);
        String resData = gameService.showMultipleCards(jsonObject);
        log.info("获取卡，返回给前端参数resData={}", resData);
        return resData;
    }

    /**
     * 测试修改状态
     */
    @RequestMapping(value = "/updateOverdueCardStatus", method = RequestMethod.POST)
    public void updateOverdueCardStatus() {
        log.info("开始查询个人卡是否过期！");
        gameService.updateOverdueCardStatus();
        log.info("修改完成！");
    }

    /**
     * 查询所有的公共消息
     *
     * @return
     */
    @RequestMapping(value = "/getAllCommonMsg")
    public RWrapper getAllCommonMsg() {
        RWrapper responseData = messageFeignService.getAllCommonMsg();
        log.info("查询所有的公共消息,返回参数={}", responseData);
        return responseData;
    }

    /**
     * 打折卡使用方法,改写,为了满足数据库事务
     * @author xuxin
     * @param
     * @return
     */
    @RequestMapping(value = "/discountCardPay")
    public String discountCardPay(HttpServletRequest request) {
        JSONObject param = RequestParamUtil.getJsonParams(request);
        log.info("使用打折卡接口参数为={}", param.toString());
        String responseData = gameService.discountCardPay(param);
        log.info("使用打折卡接口返回结果为={}", responseData);
        return responseData;
    }


}
