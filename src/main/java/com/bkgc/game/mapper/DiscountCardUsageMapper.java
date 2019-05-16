package com.bkgc.game.mapper;

import com.bkgc.bean.game.DiscountCardUsage;

public interface DiscountCardUsageMapper {
    int deleteByPrimaryKey(String id);

    int insert(DiscountCardUsage record);

    int insertSelective(DiscountCardUsage record);

    DiscountCardUsage selectByPrimaryKey(String id);
    DiscountCardUsage selectByCardId(String cardId);

    int updateByPrimaryKeySelective(DiscountCardUsage record);

    int updateByPrimaryKey(DiscountCardUsage record);

    /**
     * 根据卡包Id删除记录
     *
     * @param cardId
     * @return
     */
    int deleteByCardId(String cardId);

    int queryDiscountAmountByDate(String date);
}