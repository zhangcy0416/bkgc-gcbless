package com.bkgc.bless.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.bless.BlessEnvelopeVaild;


public interface BlessEnvelopeVaildMapper {
    int deleteByPrimaryKey(@Param("id")String id);

    int insert(BlessEnvelopeVaild record);

    int insertSelective(BlessEnvelopeVaild record);

    int insertBlessEnvelopeVaildList(List<BlessEnvelopeVaild> list);

    BlessEnvelopeVaild selectByPrimaryKey(@Param("id")String id);

	List<BlessEnvelopeVaild> getByGroupId(@Param("groupid")String groupid);
	
	List<String> getTimeByGroupId(@Param("groupid")String groupid);

}