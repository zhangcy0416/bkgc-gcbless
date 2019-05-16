package com.bkgc.bless.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.machine.ApkVersion;
import com.bkgc.bless.mapper.ApkVersionMapper;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.ResultUtil;
import com.bkgc.common.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ApkVersionService {
    @Autowired
    private ApkVersionMapper apkVersionMapper;

    /**
     * 通过版本号和渠道查找最新版本
     *
     * @param params
     * @return
     */
    public JSONObject checkVersion(JSONObject params) {
        String version = params.getString("version");
        String channel = params.getString("channel");
        if (StringUtil.isNullOrEmpty(version)) {
            throw new BusinessException(ResultCodeEnum.ERR_41045, "版本号不能为空");
        }
        if (StringUtil.isNullOrEmpty(channel)) {
            throw new BusinessException(ResultCodeEnum.ERR_41046, "渠道号不能为空");
        }

        JSONObject data = new JSONObject();

        if (!"ios".equals(channel) && !"fbtx_android".equals(channel) && version.compareTo("2.4.3") < 0) {
            data.put("result", "最新版");
            return ResultUtil.buildSuccessResult(1000, data);
        }

        ApkVersion apkVersion = apkVersionMapper.selectByChannel(channel);
        if (null == apkVersion) {
            data.put("result", "最新版");
            return ResultUtil.buildSuccessResult(1000, data);
        }
        if (version.compareTo(apkVersion.getVersion()) >= 0) {
            data.put("result", "最新版");
            return ResultUtil.buildSuccessResult(1000, data);
        } else {
            return ResultUtil.buildSuccessResult(1000, apkVersion);
        }
    }

    public JSONObject checkNewVersion(JSONObject params) {
        String version = params.getString("version");
        String channel = params.getString("channel");

        if (StringUtil.isNullOrEmpty(version)) {
            throw new BusinessException(ResultCodeEnum.ERR_41045, "版本号不能为空");
        }
        if (StringUtil.isNullOrEmpty(channel)) {
            throw new BusinessException(ResultCodeEnum.ERR_41046, "渠道号不能为空");
        }

        JSONObject data = new JSONObject();

        if (!"fbtx_ios".equals(channel) && version.compareTo("2.4.3") < 0) {
            data.put("result", "最新版");
            return ResultUtil.buildSuccessResult(1000, data);
        }

        ApkVersion apkVersion = apkVersionMapper.selectNewVersionByChannel(channel);
        if (null == apkVersion) {
            data.put("result", "最新版");
            return ResultUtil.buildSuccessResult(1000, data);
        }
        if (version.compareTo(apkVersion.getVersion()) >= 0) {
            data.put("result", "最新版");
            return ResultUtil.buildSuccessResult(1000, data);
        } else {
            return ResultUtil.buildSuccessResult(1000, apkVersion);
        }
    }

    /**
     * 查询ios最大版本
     *
     * @return
     */
    public String getMaxVersion() {

        return apkVersionMapper.selectMaxVersion4Ios().getVersion();
    }

    public ApkVersion getMaxVersion(String maxVersion) {

        return apkVersionMapper.selectMaxVersion4IosShow(maxVersion);
    }

}
