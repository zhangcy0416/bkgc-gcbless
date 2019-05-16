package com.bkgc.game.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.game.RewardPrincipal;

public interface RewardPrincipalMapper {
    int deleteByPrimaryKey(String id);

    int insert(RewardPrincipal record);

    int insertSelective(RewardPrincipal record);

    RewardPrincipal selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(RewardPrincipal record);

    int updateByPrimaryKey(RewardPrincipal record);

	RewardPrincipal selectByFactor(@Param("factor") BigDecimal b);

	List<RewardPrincipal> selectByGuid(String guid);
}