package com.bkgc.bless.mapper;

import com.bkgc.bean.user.AuthSpaceMaster;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhouyuzhao
 * @date 2018/11/26
 */
public interface AuthSpaceMasterMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(AuthSpaceMaster record);

    int insertSelective(AuthSpaceMaster record);

    AuthSpaceMaster selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AuthSpaceMaster record);

    int updateByPrimaryKey(AuthSpaceMaster record);

    /**
     * 通过手机号查询空间主信息
     *
     * @param phone
     * @return
     */
    AuthSpaceMaster selectByPhone(@Param("phone") String phone);

    /**
     * 通过guid查询空间主信息
     *
     * @param userId
     * @return
     */
    AuthSpaceMaster selectByUserId(@Param("userId") String userId);

    /**
     * 通过unionId查询空间主信息
     *
     * @param unionId
     * @return
     */
    AuthSpaceMaster selectByUnionId(@Param("unionId") String unionId);

    /**
     * 查询空间主接口(福包天下管理系统使用)
     *
     * @param authSpaceMaster
     * @return
     */
    List<AuthSpaceMaster> querySpaceMaster(AuthSpaceMaster authSpaceMaster);

    /**
     * 根据userId修改状态
     *
     * @param userId
     * @param status
     */
    void updateStatus(@Param("userId") String userId, @Param("status") Integer status);

}
