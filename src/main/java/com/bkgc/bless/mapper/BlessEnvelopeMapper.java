package com.bkgc.bless.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.bless.BlessEnvelope;
import com.bkgc.bean.bless.BlessReciver;
import com.bkgc.bean.bless.ReceiveBlessInfo;
import com.bkgc.bean.bless.ReceivedBlessEnvelope;
import com.bkgc.bean.order.OrderDailyStatistics;
import com.bkgc.bean.order.OrderSerachParam;

public interface BlessEnvelopeMapper {
    int deleteByPrimaryKey(@Param("id")String id);

    int insert(BlessEnvelope record);

    int insertSelective(BlessEnvelope record);

    BlessEnvelope selectByPrimaryKey(@Param("id")String id);

    int updateByPrimaryKeySelective(BlessEnvelope record);

    int updateByPrimaryKey(BlessEnvelope record);

	int countBlessEnvelope(BlessEnvelope blessEnvelope);

	int getCountByGroupId(@Param("groupid")String groupid);

	int getReceivedNumByGroupId(@Param("groupid")String groupid);

	List<BlessEnvelope> getByGroupId(@Param("groupid")String groupid);
	
	List<ReceiveBlessInfo> getBlessInfoByGroupId(BlessEnvelope b);

	int countReceivedThisday(BlessEnvelope blessEnvelope);

	ReceivedBlessEnvelope getReceiviableByGroupId(@Param("groupid")String groupid);

	ReceivedBlessEnvelope getReceiviableByBeId(@Param("beId")String beId);

	List<BlessEnvelope> queryByBlessEnvlope(BlessEnvelope condition);
	
	List<OrderDailyStatistics> getDailyStatisticsByMachines(OrderSerachParam param);
	
	
	List<OrderDailyStatistics> queryByGroupIds(BlessEnvelope condition);
	
	List<BlessEnvelope> queryByCondition(BlessEnvelope condition);

	List<ReceivedBlessEnvelope> getReceivedBes(BlessEnvelope condition);

	int getUnreceivedCountByGroupId(@Param("groupid")String groupid);

	List<ReceivedBlessEnvelope> getUnreceivedByGroupId(@Param("groupid")String groupid);

	int checkReceivedNumThisDay(BlessEnvelope condition);

	BlessEnvelope getEnvlopeThisDay(BlessEnvelope condition);

	List<BlessReciver> blessReciverTop(Integer number);

	BlessEnvelope getBlessInfo(String id);

	int getBlessCountByGroupId(BlessEnvelope b);
}