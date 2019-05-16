package com.bkgc.game.service;

/**
 * @author zhouyuzhao
 * @date 2018/9/5
 */
public interface BackCardService {

    /**
     * 修改卡的信息
     *
     * @param cardId
     * @param userId
     */
    void updateCardStatus(String cardId, String userId);

    /**
     * 退款的时候因子表的购彩金额减去
     *
     * @param userId
     * @param refundAmount
     */
    void updateFactor(String userId, String refundAmount);
}
