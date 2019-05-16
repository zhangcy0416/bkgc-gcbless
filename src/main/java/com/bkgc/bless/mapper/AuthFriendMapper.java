package com.bkgc.bless.mapper;

import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.user.AuthFriend;

public interface AuthFriendMapper {
    int deleteByPrimaryKey(@Param("id")String id);

    int insert(AuthFriend record);

    int insertSelective(AuthFriend record);

    AuthFriend selectByPrimaryKey(@Param("id")String id);

    int updateByPrimaryKeySelective(AuthFriend record);

    int updateByPrimaryKey(AuthFriend record);

	AuthFriend selectFriend(AuthFriend authFriend);
}