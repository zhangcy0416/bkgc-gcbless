package com.bkgc.bless.mapper;

import java.util.List;

import com.bkgc.bean.bless.RandomMappingGroup;

import feign.Param;

public interface RandomMappingGroupMapper {

    int insert(RandomMappingGroup record);
    
    int updateByPrimaryKeySelective(RandomMappingGroup record);
    
    List<RandomMappingGroup>  selectNoGroupId();
    
    RandomMappingGroup selectByRandomNumber(@Param("randomNumber")String randomNumber);
    
    RandomMappingGroup selectByGroupId(@Param("groupId")String groupId);
    
    Integer selectEmptyAll();


    int addRandomMapping(RandomMappingGroup record);

    int addRandomMappingList(List<RandomMappingGroup> list);

}
