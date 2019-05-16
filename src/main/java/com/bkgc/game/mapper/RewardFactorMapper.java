package com.bkgc.game.mapper;

import com.bkgc.bean.game.RewardFactor;
import com.bkgc.game.model.vo.GameBalance;
import org.apache.ibatis.annotations.Param;

public interface RewardFactorMapper {
    int deleteByPrimaryKey(String userId);

    int insert(RewardFactor record);

    int insertSelective(RewardFactor record);

    /**
     * 根据guid去因子表中查询数据
     *
     * @param userId
     * @return
     */
    RewardFactor selectByPrimaryKey(String userId);

    /**
     * 更新
     *
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(RewardFactor record);

    int updateByPrimaryKey(RewardFactor record);

    GameBalance generalGameBalance();

    GameBalance personalGameBalance(@Param("userId") String userId);


}