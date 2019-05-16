package com.bkgc.game.service.impl;

import com.bkgc.bean.game.RewardOfUser;
import com.bkgc.game.mapper.RewardOfUserMapper;
import com.bkgc.game.service.RewardOfUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>Title:      RewardOfUserServiceImpl </p>
 * <p>Description 用户卡包相关操作 </p>
 * <p>Company:    北控国彩 </p>
 *
 * @Author zhangft
 * @CreateDate 2018/7/11 下午3:10
 */
@Service
@Slf4j
public class RewardOfUserServiceImpl implements RewardOfUserService {

    @Autowired
    private RewardOfUserMapper rewardOfUserMapper;

    public List<RewardOfUser> selectExpireRewardList(Date time) {
        return rewardOfUserMapper.selectExpireRewardList(time);
    }

}
