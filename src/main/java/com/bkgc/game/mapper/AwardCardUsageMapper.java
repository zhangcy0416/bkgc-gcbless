package com.bkgc.game.mapper;

import com.bkgc.bean.game.AwardCardUsage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AwardCardUsageMapper {
    int deleteByPrimaryKey(String id);

    int insert(AwardCardUsage record);

    int insertSelective(AwardCardUsage record);

    AwardCardUsage selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AwardCardUsage record);

    int updateByPrimaryKey(AwardCardUsage record);

    List<AwardCardUsage> selectByOrderno(@Param("orderNo") String orderNo);

    int queryAwardBlessByDate(String date);
}