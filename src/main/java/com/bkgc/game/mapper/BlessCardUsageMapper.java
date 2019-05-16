package com.bkgc.game.mapper;

import com.bkgc.bean.game.BlessCardUsage;

public interface BlessCardUsageMapper {
    int deleteByPrimaryKey(String id);

    int insert(BlessCardUsage record);

    int insertSelective(BlessCardUsage record);

    BlessCardUsage selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BlessCardUsage record);

    int updateByPrimaryKey(BlessCardUsage record);
}