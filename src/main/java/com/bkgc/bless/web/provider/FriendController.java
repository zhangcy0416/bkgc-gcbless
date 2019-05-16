package com.bkgc.bless.web.provider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.service.impl.FriendService;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.StringUtil;
import com.bkgc.common.utils.WrapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouyuzhao
 */
@RestController
@RequestMapping("/friend")
@Slf4j
public class FriendController {


    @Autowired
    private FriendService friendService;

    /**
     * 查询用户
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/queryUser")
    public RWrapper<JSONArray> queryUser(@RequestBody JSONObject jsonObject, String pageNum1) {
        log.info("查询用户入参={}", jsonObject);
        String info = jsonObject.getString("info");
        String pageNum = jsonObject.getString("pageNum");
        if (StringUtil.isNullOrEmpty(info, pageNum)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "必填参数为空!");
        }
        JSONArray jsonArray = friendService.queryUser(info, pageNum);
        return WrapperUtil.ok(jsonArray);
    }


    /**
     * 根据userId获取用户信息(供其他服务使用)
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/getUserInfo")
    public RWrapper<JSONObject> getUserInfo(@RequestBody JSONObject jsonObject) {
        log.info("根据userId获取用户信息(供其他服务使用)入参={}", jsonObject);
        String userId = jsonObject.getString("userId");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "userId不可以为空!");
        }
        JSONObject json = friendService.getUserInfo(userId);
        return WrapperUtil.ok(json);
    }

    /**
     * 根据userId获取修改title
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/updateTitle")
    public RWrapper updateTitle(@RequestBody JSONObject jsonObject) {
        log.info("根据userId获取修改title入参={}", jsonObject);
        String userId = jsonObject.getString("userId");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "userId不可以为空!");
        }
        friendService.updateTitle(userId);
        log.info("修改title成功!");
        return WrapperUtil.ok();
    }


}
