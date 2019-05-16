package com.bkgc.game.mapper;

import com.bkgc.bean.game.Reward;

import java.util.List;

public interface RewardMapper {
    int deleteByPrimaryKey(String id);

    int insert(Reward record);

    int insertSelective(Reward record);

    /**
     * 根据奖品Id查询奖品
     *
     * @param id
     * @return
     */
    Reward selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Reward record);

    int updateByPrimaryKey(Reward record);

    List<Reward> selectByRewardType(String rewardType);

    List<Reward> selectByReward(Reward record);

    List<Reward> queryAll();
}