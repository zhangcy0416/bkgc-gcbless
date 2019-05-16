package com.bkgc.game.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.game.DiscountCardUsage;
import com.bkgc.bean.game.RewardFactor;
import com.bkgc.bean.game.RewardOfUser;
import com.bkgc.game.mapper.DiscountCardUsageMapper;
import com.bkgc.game.mapper.RewardFactorMapper;
import com.bkgc.game.mapper.RewardOfUserMapper;
import com.bkgc.game.message.UserAppPushMessage;
import com.bkgc.game.service.BackCardService;
import com.xiaoleilu.hutool.date.DateUnit;
import com.xiaoleilu.hutool.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zhouyuzhao
 * @date 2018/9/5
 */
@Slf4j
@Service
public class BackCardServiceImpl implements BackCardService {

    @Autowired
    private RewardOfUserMapper rewardOfUserMapper;

    @Autowired
    private DiscountCardUsageMapper discountCardUsageMapper;

    @Autowired
    private UserAppPushMessage userAppPushMessage;

    @Autowired
    private RewardFactorMapper rewardFactorMapper;

    @Override
    public void updateCardStatus(String cardId, String userId) {
        RewardOfUser rewardOfUser = rewardOfUserMapper.selectByPrimaryKey(cardId);
        log.info("根据卡包ID查询卡包={}", rewardOfUser);
        //过期时间
        Date expiryTime = DateUtil.parse(DateUtil.formatDateTime(rewardOfUser.getExpired()));
        //使用时间
        Date useTime = DateUtil.parse(DateUtil.formatDateTime(rewardOfUser.getUseTime()));
        //退卡时间
        Date backCard = DateUtil.parseDate(DateUtil.formatDate(new Date()));
        backCard = DateUtil.offsiteDay(backCard, 1);
        //Date backCard = DateUtil.parseDate("2018-08-20");
        log.info("过期时间={}", expiryTime);
        log.info("使用时间={}", useTime);
        log.info("退卡时间={}", backCard);
        Long days = DateUtil.between(expiryTime, useTime, DateUnit.DAY) + 1;
        log.info("还有{}天过期！", days);
        //新的过期时间
        Date newExpiryTime = DateUtil.offsiteDay(backCard, days.intValue());
        log.info("新的过期时间={}！", newExpiryTime);

        rewardOfUser.setStatus(0);
        rewardOfUser.setExpired(newExpiryTime);
        rewardOfUser.setUseTime(null);
        rewardOfUserMapper.updateByPrimaryKey(rewardOfUser);
        log.info("更新卡包表成功!");
        DiscountCardUsage discountCardUsage = discountCardUsageMapper.selectByCardId(cardId);
        log.info("discountCardUsage{}", discountCardUsage);
        if (discountCardUsage != null) {
            Long discountAmount = discountCardUsage.getDiscountAmount();
            if (discountAmount != null) {
                RewardFactor rewardFactor = rewardFactorMapper.selectByPrimaryKey(userId);
                if (rewardFactor != null) {
                    BigDecimal gameRewardPrice = rewardFactor.getGameRewardPrice();
                    BigDecimal gameRewardPriceAll = gameRewardPrice.subtract(BigDecimal.valueOf(discountAmount));
                    rewardFactor.setGameRewardPrice(gameRewardPriceAll);
                    rewardFactorMapper.updateByPrimaryKey(rewardFactor);
                    log.info("更新因子表成功！====");
                }
            }
        }

        discountCardUsageMapper.deleteByCardId(cardId);
        log.info("删除使用记录成功!");
        sendAppMessage(userId);
    }

    @Override
    public void updateFactor(String userId, String refundAmount) {
        RewardFactor rewardFactor = rewardFactorMapper.selectByPrimaryKey(userId);
        log.info("rewardFactor={}", rewardFactor);
        if (rewardFactor != null) {
            BigDecimal lotteryAmount = rewardFactor.getPurchesLotteryAmount();
            BigDecimal purLotteryAmount = lotteryAmount.subtract(new BigDecimal(refundAmount));
            rewardFactor.setPurchesLotteryAmount(purLotteryAmount);
            rewardFactorMapper.updateByPrimaryKey(rewardFactor);
            log.info("更新因子表成功！");
        }
    }


    /**
     * 向国彩福包app推送退卡消息
     */
    public void sendAppMessage(String userId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        jsonObject.put("alert", "您有一张津贴卡退回");
        jsonObject.put("title", "退卡通知");
        log.info("将要发送app消息，参数={}", jsonObject.toString());
        boolean isSend = userAppPushMessage.pushUserCardMessage(jsonObject);
        if (isSend) {
            log.info("给用户推送退款消息成功！");
        } else {
            log.info("给用户推送退款消息失败！");
        }
    }

}
