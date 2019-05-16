package com.bkgc.bless.mapper;

import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.bless.AuthLoginCredential;


public interface AuthLoginCredentialMapper {
    int deleteByPrimaryKey(@Param("id")String id);

    int insert(AuthLoginCredential record);

    int insertSelective(AuthLoginCredential record);

    AuthLoginCredential selectByPrimaryKey(@Param("id")String id);

    int updateByPrimaryKeySelective(AuthLoginCredential record);

    int updateByPrimaryKey(AuthLoginCredential record);

	AuthLoginCredential getLoginInfoByUserName(@Param("userName")String userName);
	
	AuthLoginCredential getLoginInfoByUserId(@Param("userid")String userid);

	void updateByUserId(AuthLoginCredential authLoginCredential);
	
	int getCount(AuthLoginCredential record);
}