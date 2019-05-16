package com.bkgc.game.util;

/**
 * Created by admin on 2017/12/28.
 */
public enum RewardCodeEnum {

    TWO_BLESS("TWO_BLESS"), FIVE_BLESS("TWO_BLESS"), TEN_BLESS("TWO_BLESS"),
    NINETY_DISCOUNT("NINETY_DISCOUNT"), EIGHTY_DISCOUNT("EIGHTY_DISCOUNT"), PORTABLE_SOURCE("PORTABLE_SOURCE"),
    LUXURY("LUXURY"), DOUBLE_AWARD("DOUBLE_AWARD"), TREBLE_AWARD("TREBLE_AWARD");
    //ninety_discount

    private String rewardCode;

    RewardCodeEnum(String rewardCode) {
        this.rewardCode = rewardCode;
    }

    public String getRewardCode() {
        return rewardCode;
    }

    public void setRewardCode(String rewardCode) {
        this.rewardCode = rewardCode;
    }


}
