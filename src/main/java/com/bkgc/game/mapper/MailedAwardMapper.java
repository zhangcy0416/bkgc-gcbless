package com.bkgc.game.mapper;

import com.bkgc.bean.game.MailRewardOrder;
import com.bkgc.bean.game.MailedAward;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MailedAwardMapper {
    int deleteByPrimaryKey(String id);

    int insert(MailedAward record);

    int insertSelective(MailedAward record);

    MailedAward selectByPrimaryKey(String id);
    
    MailedAward selectByCardId(String cardId);

    int updateByPrimaryKeySelective(MailedAward record);

    int updateByPrimaryKey(MailedAward record);

    List<MailRewardOrder> getmailRewardOrderList(MailRewardOrder mailRewardOrder);

    Integer getmailRewardOrderListCount(MailRewardOrder mailRewardOrder);

    MailedAward selectByCardIdAndUserId(@Param("cardId") String cardId, @Param("userId") String userId);
}