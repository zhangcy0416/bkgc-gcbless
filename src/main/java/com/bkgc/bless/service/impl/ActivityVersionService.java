package com.bkgc.bless.service.impl;

import com.bkgc.bean.bless.ActivityVersion;
import com.bkgc.bless.mapper.ActivityVersionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityVersionService {

    @Autowired
    private ActivityVersionMapper activityVersionMapper;

    public ActivityVersion getActivityVersionInfo(String activityId, String version) {
        return activityVersionMapper.queryActivityInfoByVersion(activityId, version);
    }

}
