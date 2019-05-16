package com.bkgc.bless.mapper;

import com.bkgc.bean.machine.ApkVersion;

public interface ApkVersionMapper {

    ApkVersion selectByChannel(String channel);

    ApkVersion selectNewVersionByChannel(String channel);

    ApkVersion selectMaxVersion4Ios();

    ApkVersion selectMaxVersion4IosShow(String maxVersion);


}