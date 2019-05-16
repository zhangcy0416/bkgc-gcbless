package com.bkgc.game.service;

import com.bkgc.bean.game.RewardOfUser;

import java.util.Date;
import java.util.List;

/**
 * Created by zhangft on 2018/7/11.
 */
public interface RewardOfUserService {

    List<RewardOfUser> selectExpireRewardList(Date time);
}
