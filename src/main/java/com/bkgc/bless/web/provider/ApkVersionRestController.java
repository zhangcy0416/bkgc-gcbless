package com.bkgc.bless.web.provider;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.service.impl.ApkVersionService;
import com.bkgc.common.utils.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by zhangft on 2018/7/3.
 */
@RestController
@RequestMapping("/version")
@Slf4j
public class ApkVersionRestController {

    @Autowired
    private ApkVersionService apkVersionService;

    /**
     * 通过版本号和渠道查找最新版本
     *
     * @return
     */
    @RequestMapping(value = "/checkVersion")
    public JSONObject checkVersion(@RequestBody JSONObject requestParam) {
        log.info("checkVersion方法入参{}", requestParam);
        JSONObject responseData = apkVersionService.checkVersion(requestParam);
        log.info("checkVersion方法出参{}", responseData);
        return responseData;
    }


    /**
     * 查询ios最大版本
     *
     * @return
     */
    @RequestMapping(value = "/ios/hidden")
    public JSONObject ios() {
        String maxVersion = apkVersionService.getMaxVersion();
        log.info("ios最大版本号{}", maxVersion);
        return ResultUtil.buildSuccessResult(1000, maxVersion);
    }

}
