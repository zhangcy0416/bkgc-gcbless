package com.bkgc.game.mapper;

import com.bkgc.bean.game.RewardOfUser;
import com.bkgc.bean.game.dto.RewardOfUserDto;
import com.bkgc.game.model.vo.GameBlessVo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface RewardOfUserMapper {
    int deleteByPrimaryKey(String id);

    int insert(RewardOfUser record);

    int insertSelective(RewardOfUser record);

    /**
     * 根据卡包ID获取卡包
     *
     * @param id
     * @return
     */
    RewardOfUser selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(RewardOfUser record);

    int updateByPrimaryKey(RewardOfUser record);

    List<RewardOfUser> selectByuserId(String guid);

    List<RewardOfUser> selectByRewardType(String type);

    List<RewardOfUser> selectByObject(RewardOfUser rewardOfUser);

    int getRewardCount(String userId);

    /**
     * 根据guid获取个人所有得到的奖品 获取时间倒序
     *
     * @param guid
     * @return
     */
    List<RewardOfUser> selectRewardsByUserId(String guid);

    /**
     * 兑奖成功  展示的翻倍卡和三倍卡
     *
     * @param guid
     * @return
     */
    List<RewardOfUser> showMultipleCards(String guid);

    List<RewardOfUser> selectExpireRewardList(Date time);

    /**
     * 根据guid和卡包的类型获得未使用的卡
     *
     * @param guid
     * @param rewardType
     * @return
     */
    List<RewardOfUser> showMultipleCards(@Param("guid") String guid, @Param("rewardType") String rewardType);

    /**
     * 查询出所有未使用的卡
     */
    List<RewardOfUser> getAllNotUsedCard();

    List<RewardOfUserDto> selectRewardOfUser(@Param("guid") String guid, @Param("condition") String condition, @Param("status") Integer status);

    List<RewardOfUserDto> selectRewardOfUserList(RewardOfUserDto rewardOfUserDto);

    List<GameBlessVo> queryGameBlessList(@Param("date") String date);
}