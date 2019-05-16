package com.bkgc.game.mapper;

import com.bkgc.bean.game.AuthAddress;

public interface AuthAddressMapper {
    int deleteByPrimaryKey(String id);

    int insert(AuthAddress record);

    int insertSelective(AuthAddress record);

    AuthAddress selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AuthAddress record);

    int updateByPrimaryKey(AuthAddress record);

	AuthAddress selectByGuid(String guid);

}