package com.bkgc.game.service;

import com.bkgc.bean.game.RewardFactor;

/**
 * Created by admin on 2017/12/27.
 */
public interface RewardFactorService {

    RewardFactor getByUserId(String userId);

    int updateByUserId(RewardFactor rewardFactor);

    int insertSelective(RewardFactor rewardFactor);

}
