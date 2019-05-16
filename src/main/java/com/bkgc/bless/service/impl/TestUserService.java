package com.bkgc.bless.service.impl;

import com.bkgc.bless.mapper.TestUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestUserService {

    @Autowired
    private TestUserMapper testUserMapper;

    public int isTestUser(String userId){
        return testUserMapper.isTestUser(userId);
    }
}
