package com.bkgc.bless.mapper;

import java.util.Date;
import java.util.List;

import com.bkgc.bean.bless.*;
import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.order.OrderDailyStatistics;
import com.bkgc.bean.order.OrderSerachParam;

public interface BlessEnvelopeGroupMapper {
    int deleteByPrimaryKey(@Param("id")String id);

    int insert(BlessEnvelopeGroup record);

    int insertSelective(BlessEnvelopeGroup record);

    BlessEnvelopeGroup selectByPrimaryKey(@Param("id")String id);
    
    int updateByPrimaryKeySelective(BlessEnvelopeGroup record);

    int updateByPrimaryKey(BlessEnvelopeGroup record);

	int countBlessEnvelopeGroup(BlessEnvelopeGroup blessEnvelopeGroup);
	
	
	int aliveCount(@Param("id")String id);

	int getCountByMachineId(String machineId);
	
	List<BlessEnvelopeGroup> getGroup(BlessEnvelopeGroup blessEnvelopeGroup);
	
	List<BlessEnvelopeGroup> getAliveGroups(BlessEnvelopeGroup blessEnvelopeGroup);
	
	OrderDailyStatistics queryByGroupIds(BlessEnvelopeGroup blessEnvelopeGroup);
	
	List<BlessEnvelopeGroup> checkGroupExpired(Date time);
	
	List<OrderDailyStatistics> getDailyStatisticsByGroupIds(OrderSerachParam param);

	List<BlessEnvelopeGroup> getBlessGroup(BlessEnvelopeGroup b);
	
	int getBlessGroupCount(BlessEnvelopeGroup b);

	List<OrderDailyStatistics> getGroupStatistics(OrderSerachParam param);

	BlessGroupParam getStatisticsByMachineIds(List<Integer> list);
	
	BlessInfo getBlessGroupInfo(@Param("id")String id);

//	int getExpireBlessGroupCount(BlessEnvelopeGroup b);
//
	List<BlessEnvelopeGroup> getTop10();

	List<BlessEnvelopeGroupMachineR> getBlessenvelopeByArea();
}