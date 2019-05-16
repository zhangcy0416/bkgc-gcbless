package com.bkgc.bless.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.bless.BlessEnvelopeGroupGrands;


public interface BlessEnvelopeGroupGrandsMapper {

	BlessEnvelopeGroupGrands selectByPrimaryKey(@Param("id")String id);
	
	int deleteByPrimaryKey(@Param("id")String id);
	
    int insertSelective(BlessEnvelopeGroupGrands grand);

    int insertGroupGrandList(List<BlessEnvelopeGroupGrands> list);

    int updateByPrimaryKeySelective(BlessEnvelopeGroupGrands grand);
    
    
    List<BlessEnvelopeGroupGrands> selectByGroupId(String groupid);
    
    
	
}
