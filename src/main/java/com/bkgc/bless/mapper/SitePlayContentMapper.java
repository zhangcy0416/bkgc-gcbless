package com.bkgc.bless.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.bkgc.bean.bless.SitePlayContent;


public interface SitePlayContentMapper {
    int deleteByPrimaryKey(@Param("id")String id);

    int insert(SitePlayContent record);

    int insertSelective(SitePlayContent record);

    SitePlayContent selectByPrimaryKey(@Param("id")String id);

    int updateByPrimaryKeySelective(SitePlayContent record);

    int updateByPrimaryKey(SitePlayContent record);
    
    List<SitePlayContent> selectByContent(SitePlayContent record);
}