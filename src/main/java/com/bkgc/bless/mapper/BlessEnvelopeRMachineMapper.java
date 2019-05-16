package com.bkgc.bless.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.bless.BlessEnvelopeRMachine;

public interface BlessEnvelopeRMachineMapper {
    int deleteByPrimaryKey(@Param("id")Integer id);

    int insert(BlessEnvelopeRMachine record);

    int insertSelective(BlessEnvelopeRMachine record);

    BlessEnvelopeRMachine selectByPrimaryKey(@Param("id")Integer id);

    int updateByPrimaryKeySelective(BlessEnvelopeRMachine record);

    int updateByPrimaryKey(BlessEnvelopeRMachine record);

	List<BlessEnvelopeRMachine> getByMachineId(@Param("machineid")Integer machineid);
}