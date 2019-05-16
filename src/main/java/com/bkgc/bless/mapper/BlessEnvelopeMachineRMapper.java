package com.bkgc.bless.mapper;

import java.util.List;

import com.bkgc.bean.bless.BlessEnvelopeGroupMachineR;


public interface BlessEnvelopeMachineRMapper {
    int deleteByPrimaryKey(Integer id);
    
    BlessEnvelopeGroupMachineR queryByPrimaryKey(String id);

    int insert(BlessEnvelopeGroupMachineR record);
    
	void update(BlessEnvelopeGroupMachineR r);
	void updateByPrimaryKeySelective(BlessEnvelopeGroupMachineR r);

	int updateStatusByGroupId(BlessEnvelopeGroupMachineR r);

	void add(BlessEnvelopeGroupMachineR r);

	void addEnvelopeMachineList(List<BlessEnvelopeGroupMachineR> list);

	List<BlessEnvelopeGroupMachineR> queryByBlessEnvelopeGroupId(String blessEnvelopeGroupId);

	void deleteByBlessEnvelopeGroupId(String id);

	List<BlessEnvelopeGroupMachineR> queryByMachineId(Integer id);
}