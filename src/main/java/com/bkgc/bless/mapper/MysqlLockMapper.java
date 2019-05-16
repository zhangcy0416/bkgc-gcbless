package com.bkgc.bless.mapper;


import com.bkgc.bean.bless.MysqlLock;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface MysqlLockMapper  {

    int addMysqlLock(@Param("id") String id, @Param("randStr") String randStr, @Param("createTime") Date createTime, @Param("expireTime") Date expireTime);

    MysqlLock queryMysqlLock(@Param("id") String id);

    int delMysqlLock(@Param("id") String id,@Param("randStr") String randStr);

    int delMysqlLock4Expire(@Param("id") String id);
}