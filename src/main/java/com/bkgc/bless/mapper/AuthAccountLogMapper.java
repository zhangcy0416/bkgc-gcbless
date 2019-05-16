package com.bkgc.bless.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.account.AccountWithdraw;
import com.bkgc.bean.account.AuthAccountLog;


public interface AuthAccountLogMapper {
    int deleteByPrimaryKey(@Param("id")String id);

    int insert(AuthAccountLog record);

    int insertSelective(AuthAccountLog record);

    AuthAccountLog selectByPrimaryKey(@Param("id")String id);

    int updateByPrimaryKeySelective(AuthAccountLog record);

    int updateByPrimaryKey(AuthAccountLog record);

	List<AuthAccountLog> searchRecords(AuthAccountLog accountLog);

	int count(AuthAccountLog accountLog);

	BigDecimal getSumAmount(AuthAccountLog accountLog);

	List<AuthAccountLog> getDepositRecord(AuthAccountLog accountLog);

	int countDepositRecord(AuthAccountLog accountLog);

	List<AuthAccountLog> getWithdrawRecord(AuthAccountLog accountLog);

	int countWithdrawRecord(AuthAccountLog accountLog);

	int countLotteryAwardRecord(AuthAccountLog accountLog);

	List<AuthAccountLog> getLotteryAwardRecord(AuthAccountLog accountLog);

	AuthAccountLog selectByRemark(@Param("remark")String remark);

	int countWithdraw(AccountWithdraw withdraw);
}