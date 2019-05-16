package com.bkgc.bless.mapper;


import com.bkgc.bean.bless.ActivityVersion;
import org.apache.ibatis.annotations.Param;

public interface ActivityVersionMapper  {

    ActivityVersion queryActivityInfoByVersion(@Param("activityId") String activityId,@Param("version") String version);
}