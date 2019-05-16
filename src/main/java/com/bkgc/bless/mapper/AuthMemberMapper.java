package com.bkgc.bless.mapper;

import com.bkgc.bean.SearchBean;
import com.bkgc.bean.user.AuthMember;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuthMemberMapper {
    int deleteByPrimaryKey(@Param("id") String id);

    int insert(AuthMember record);

    int insertSelective(AuthMember record);

    AuthMember selectByPrimaryKey(@Param("id") String id);

    int updateByPrimaryKeySelective(AuthMember record);

    int updateByPrimaryKey(AuthMember record);

    /**
     * auth_member表中
     * 通过手机号查询用户是否存在
     *
     * @param phone
     * @return
     */
    AuthMember getAuthMemberByPhone(@Param("phone") String phone);

    AuthMember getAuthMemberByWXOpenId(@Param("openId") String openId);


    AuthMember getAuthMemberByUnionId(@Param("unionId") String unionId);

    /**
     * 兼容国彩福包app
     *
     * @param unionId
     * @return
     */
    AuthMember getNewAuthMemberByUnionId(@Param("unionId") String unionId);

    /**
     * 根据unionId和角色查询
     *
     * @param unionId
     * @param roleId
     * @return
     */
    AuthMember getAuthMemberByUnionIdAndRole(@Param("unionId") String unionId, @Param("roleId") int roleId);

    AuthMember getByNickName(@Param("nickName") String nickName);


    int getCount(@Param("id") String id);

    Integer getAllCount(SearchBean searchBean);

    int checkIsBound(@Param("phone") String phone);

    AuthMember getNotBindWXMember(@Param("phone") String phone);

    AuthMember getNotBindQQMember(@Param("phone") String phone);

    int makeItUnaccessible(@Param("id") String id);

    List<AuthMember> getAll();

    List<AuthMember> getPageAll(SearchBean pageBean);

    AuthMember selectByguid(@Param("guid") String guid);

    List<AuthMember> getSearched(SearchBean pageBean);

    Integer getMembersCount(SearchBean pageBean);

    /**
     * auth_member表中
     * 通过手机号和角色查询用户是否存在  6：国彩福包用户 8：福包天下用户
     *
     * @param phone
     * @param roleId
     * @return
     */
    AuthMember getAuthMemberByPhoneAndRole(@Param("phone") String phone, @Param("roleId") int roleId);

    /**
     * 兼容国彩福包app的方法
     *
     * @param phone
     * @return
     */
    AuthMember getNewAuthMemberByPhone(@Param("phone") String phone);

    /**
     * 搜索用户  用户被禁用搜索不出来
     *
     * @param info
     * @return
     */
    List<AuthMember> searchUser(@Param("info") String info);

    /**
     * 查询用户接口(福包天下管理系统使用)
     *
     * @param authMember
     * @return
     */
    List<AuthMember> queryAuthMember(AuthMember authMember);


    /**
     * 根据userId修改状态
     *
     * @param userId
     * @param status
     */
    void updateStatus(@Param("userId") String userId, @Param("status") Integer status);

    /**
     * 根据userId修改title 称号:0:无称号、1:公益使者、2:公益达人、3:公益大使'
     *
     * @param userId
     * @param title
     */
    void updateTitle(@Param("userId") String userId, @Param("title") Integer title);

}