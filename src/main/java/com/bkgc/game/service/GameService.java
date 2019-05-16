package com.bkgc.game.service;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.game.MailRewardOrder;
import com.bkgc.bean.game.MailedAward;
import com.bkgc.common.utils.RWrapper;

import java.util.Map;

/**
 * 该类为福包夺宝游戏主要接口, 主要包括开始夺宝游戏, 领取游戏奖品, 使用奖品
 * Created by yanqiang on 2017/12/21.
 */
public interface GameService {


    /**
     * 开始游戏, 此方法包括以下功能: 调用福包支付,当支付成功后加载游戏规则,
     * 游戏规则会返回一个确定的奖品id, 获取到此id后连同guid一起返回给游戏页面
     *
     * @param guid 用户id
     * @return
     * @throws Exception
     */
    //String startGame(String guid) throws RuntimeException;

    String gameStart(String guid);

    /**
     * 领取奖品, 夺宝游戏一轮结束后需要保存用户获得的奖品至数据库.
     * 奖品不同,领取奖品的流程也不同,根据设计的奖品类别领取流程实现该方法
     *
     * @param guid     用户id
     * @param rewardId 奖品id
     * @return
     * @throws Exception
     */
    JSONObject saveReward(String guid, String rewardId);


    /**
     * 打折卡使用方法,具体流程参考文档
     *
     * @param param
     * @return
     * @throws Exception
     */
    String useDiscountCard(JSONObject param) throws RuntimeException;


    /**
     * 奖金翻倍卡使用方法, 具体流程参考文档
     *
     * @param param
     * @return
     * @throws Exception
     */
    String useAwardCard(JSONObject param);


    /**
     * 使用实物寄送的奖品,具体流程参考文档
     *
     * @param param
     * @return
     * @throws Exception
     */
    String useMailedAward(JSONObject param) throws RuntimeException;


    RWrapper getReward(JSONObject jsonObject);

    RWrapper getAddress(JSONObject param);


    /**
     * 获取有效期内的未翻倍的订单
     *
     * @param jsonObject
     * @return
     */
    String getValidAwardList(JSONObject jsonObject);


    String updateAddress(JSONObject jsonObject);


    String getAliveReward(JSONObject jsonObject);

    Map<String, Object> getmailRewardOrderList(MailRewardOrder mailRewardOrder);//获取实物邮寄订单列表

    RWrapper updateByPrimaryKeySelective(MailedAward record);//添加快递单号

    RWrapper getAwardList();

    /**
     * 个人中心----夺宝记录
     *
     * @param jsonObject
     * @return
     */
    String rewardRecord(JSONObject jsonObject);

    /**
     * 更新配送信息
     *
     * @param param
     * @return
     * @throws Exception
     */
    String updateUseMailedAward(JSONObject param);

    /**
     * 获取配送信息
     *
     * @param param
     * @return
     */
    RWrapper getUseMailedAward(JSONObject param);

    /**
     * 兑奖成功  展示的翻倍卡和三倍卡
     *
     * @param jsonObject
     * @return
     */
    String showMultipleCards(JSONObject jsonObject);

    /**
     * 修改过期卡包的状态
     */
    void updateOverdueCardStatus();

    /**
     * 打折卡使用方法,具体流程参考文档
     * @author xuxin
     * @Date  2018-07-19
     * @param param
     * @return
     * @throws Exception
     */
    String discountCardPay(JSONObject param);

    JSONObject queryGameBless(String date);

}
