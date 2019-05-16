package com.bkgc.game.service.impl;

import com.bkgc.bean.game.RewardFactor;
import com.bkgc.game.mapper.RewardFactorMapper;
import com.bkgc.game.service.RewardFactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by admin on 2017/12/27.
 */
@Service(value = "rewardFactorService")
public class RewardFactorServiceImpl implements RewardFactorService {

    @Autowired
    private RewardFactorMapper rewardFactorMapper;

//    private static RewardFactorMapper staticRewardFactorMapper;

    private static RewardFactorServiceImpl RewardFactorService;

    @PostConstruct
    public void initRewardFactorMapper(){
        RewardFactorService = this;
    }

    public RewardFactor getByUserId(String userId){
          return rewardFactorMapper.selectByPrimaryKey(userId);
    }

    public int updateByUserId(RewardFactor rewardFactor){
        return rewardFactorMapper.updateByPrimaryKeySelective(rewardFactor);
    }

    public int insertSelective(RewardFactor rewardFactor){
        return rewardFactorMapper.insertSelective(rewardFactor);
    }

    public static RewardFactorServiceImpl getInstance(){
        return RewardFactorService;
    }


}
