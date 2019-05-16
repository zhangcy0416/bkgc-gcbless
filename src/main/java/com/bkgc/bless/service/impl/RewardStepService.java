package com.bkgc.bless.service.impl;

import com.bkgc.bean.bless.RewardStep;
import com.bkgc.bless.mapper.RewardStepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RewardStepService {

    @Autowired
    private RewardStepMapper rewardStepMapper;


    public List<RewardStep> queryAll(){
        return rewardStepMapper.queryAll();
    }

}
