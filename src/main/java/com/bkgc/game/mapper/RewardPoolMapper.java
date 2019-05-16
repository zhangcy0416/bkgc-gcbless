package com.bkgc.game.mapper;

import com.bkgc.bean.game.RewardPool;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RewardPoolMapper {
    int deleteByPrimaryKey(String id);

    int insert(RewardPool record);

    int insertSelective(RewardPool record);

    RewardPool selectByPrimaryKey(String id);

    int updateAmountByRewardId(String rewardId);

    List<RewardPool> selectAll();

    int updateByPrimaryKeySelective(RewardPool record);

    int updateByPrimaryKey(RewardPool record);

    RewardPool selectRewardPoolByRewardId(@Param("rewardId") String rewardId);
}