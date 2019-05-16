package com.bkgc.game.socket;

import com.bkgc.bless.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


@Service
public class StartMessageQueue {

    @Autowired
    private RedisUtil redisUtil;

    private final String REWARDLIST = "rewardList";


    public void addMessage(String message) {
        redisUtil.setRewardList(REWARDLIST, message);
    }

    public List<String> getRewardList() {
        return redisUtil.getRewardList(REWARDLIST);
    }

}
