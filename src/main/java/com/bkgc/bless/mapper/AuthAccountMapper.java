package com.bkgc.bless.mapper;

import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.account.AuthAccount;

public interface AuthAccountMapper {
    int deleteByPrimaryKey(@Param("id")String id);

    int insert(AuthAccount record);

    int insertSelective(AuthAccount record)throws Exception;

    AuthAccount selectByPrimaryKey(@Param("id")String id);

    int updateByPrimaryKeySelective(AuthAccount record);

    int updateByPrimaryKey(AuthAccount record);

	AuthAccount selectByUserId(@Param("userId")String userId);

	AuthAccount selectByCondition(AuthAccount authAccount);

}