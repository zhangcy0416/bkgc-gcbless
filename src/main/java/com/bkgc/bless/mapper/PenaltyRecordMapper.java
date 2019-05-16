package com.bkgc.bless.mapper;

import com.bkgc.bless.model.domain.PenaltyRecord;
import com.bkgc.bless.model.vo.PenaltyRecordVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhouyuzhao
 * @date 2018/12/6
 */
public interface PenaltyRecordMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PenaltyRecord record);

    int insertSelective(PenaltyRecord record);

    PenaltyRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PenaltyRecord record);

    int updateByPrimaryKey(PenaltyRecord record);

    List<PenaltyRecord> selectByUserId(@Param("userId") String userId);

    List<PenaltyRecordVo> selectPage(@Param("realName") String realName, @Param("nickName") String nickName, @Param("phone") String phone, @Param("roleId") Integer roleId);

    List<PenaltyRecordVo> selectSpacePage(@Param("realName") String realName, @Param("nickName") String nickName, @Param("phone") String phone, @Param("roleId") Integer roleId);
}
