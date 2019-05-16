package com.bkgc.bless.web.provider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bkgc.bless.service.impl.AuthMemberProviderService;
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
 * 用户管理和空间主管理接口(福包天下管理系统使用)
 *
 * @author zhouyuzhao
 * @date 2018/12/3
 */
@Slf4j
@RestController
@RequestMapping("/authMemberProvider")
public class AuthMemberProvider {

    @Autowired
    private AuthMemberProviderService authMemberProviderService;

    /**
     * 查询福包天下用户接口
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping("/queryAuthMember")
    public RWrapper<JSONObject> queryAuthMember(@RequestBody JSONObject jsonObject) {
        log.info("福包天下查询用户接口入参={}", jsonObject);
        JSONObject resJson = authMemberProviderService.queryAuthMember(jsonObject);
        log.info("福包天下查询用户接口返回数据={}", resJson);
        return WrapperUtil.ok(resJson);
    }

    /**
     * 查询空间主接口
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping("/querySpaceMaster")
    public RWrapper<JSONObject> querySpaceMaster(@RequestBody JSONObject jsonObject) {
        log.info("福包天下查询空间主接口入参={}", jsonObject);
        JSONObject json = authMemberProviderService.querySpaceMaster(jsonObject);
        log.info("福包天下查询空间主接口返回数据={}", json);
        return WrapperUtil.ok(json);
    }

    /**
     * 根据userId查询用户信息
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping("/queryAuthMemberById")
    public RWrapper<JSONObject> queryAuthMemberById(@RequestBody JSONObject jsonObject) {
        log.info("根据userId查询用户信息入参={}", jsonObject);
        String userId = jsonObject.getString("userId");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.FAIL, "用户id不能为空!");
        }
        JSONObject json = authMemberProviderService.queryAuthMemberById(userId);
        log.info("根据userId查询用户信息返回数据={}", json);
        return WrapperUtil.ok(json);
    }

    /**
     * 根据userId查询空间主信息
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping("/querySpaceMasterById")
    public RWrapper<JSONObject> querySpaceMasterById(@RequestBody JSONObject jsonObject) {
        log.info("根据userId查询空间主信息入参={}", jsonObject);
        String userId = jsonObject.getString("userId");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.FAIL, "用户id不能为空!");
        }
        JSONObject json = authMemberProviderService.querySpaceMasterById(userId);
        log.info("根据userId查询空间主信息返回数据={}", json);
        return WrapperUtil.ok(json);
    }


    /**
     * 用户升级为空间主
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping("/upgrade")
    public RWrapper upgrade(@RequestBody JSONObject jsonObject) {
        log.info("用户升级为空间主入参={}", jsonObject);
        String userId = jsonObject.getString("userId");
        String realName = jsonObject.getString("realName");
        String idCard = jsonObject.getString("idCard");
        if (StringUtil.isNullOrEmpty(userId, realName, idCard)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "必填参数为空!");
        }
        authMemberProviderService.upgrade(jsonObject);
        log.info("升级完成！");
        return WrapperUtil.ok();
    }


    /**
     * 封号、解封、禁言、解禁  sign：1、2、3、4
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping("/updateStatus")
    public RWrapper updateStatus(@RequestBody JSONObject jsonObject) {
        log.info("封号、解封、禁言、解禁入参={}", jsonObject);
        authMemberProviderService.updateStatus(jsonObject);
        return WrapperUtil.ok();
    }

    /**
     * 新增空间主
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping("/addSpaceMaster")
    public RWrapper addSpaceMaster(@RequestBody JSONObject jsonObject) {
        log.info("新增空间主入参={}", jsonObject);
        authMemberProviderService.addSpaceMaster(jsonObject);
        return WrapperUtil.ok();
    }

    /**
     * 用户和空间主的被罚记录
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping("/userPenaltyRecord")
    public RWrapper<JSONObject> userPenaltyRecord(@RequestBody JSONObject jsonObject) {
        log.info("用户和空间主的被罚记录={}", jsonObject);
        JSONObject json = authMemberProviderService.userPenaltyRecord(jsonObject);
        log.info("用户和空间主的被罚记录={}", json);
        return WrapperUtil.ok(json);
    }
}
