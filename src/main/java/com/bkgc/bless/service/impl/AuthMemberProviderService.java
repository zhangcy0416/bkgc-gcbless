package com.bkgc.bless.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendResult;
import com.bkgc.bean.user.AuthMember;
import com.bkgc.bean.user.AuthSpaceMaster;
import com.bkgc.bless.config.Config;
import com.bkgc.bless.config.MqInitializer;
import com.bkgc.bless.constant.CommonConfig;
import com.bkgc.bless.consumer.*;
import com.bkgc.bless.mapper.AuthMemberMapper;
import com.bkgc.bless.mapper.AuthSpaceMasterMapper;
import com.bkgc.bless.mapper.PenaltyRecordMapper;
import com.bkgc.bless.model.domain.PenaltyRecord;
import com.bkgc.bless.model.vo.PenaltyRecordVo;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author zhouyuzhao
 * @date 2018/12/3
 */
@Slf4j
@Service
public class AuthMemberProviderService {

    @Autowired
    private AuthMemberMapper authMemberMapper;

    @Autowired
    private AuthSpaceMasterMapper authSpaceMasterMapper;

    @Autowired
    private AuthFeignService authFeignService;
    @Resource
    private Config config;

    @Autowired
    private MessageFeignService messageFeignService;

    @Autowired
    private GtsFeignService gtsFeignService;

    @Autowired
    private PaymentFeignService paymentFeignService;

    @Autowired
    private PenaltyRecordMapper penaltyRecordMapper;

    @Autowired
    private FbtxFeignService fbtxFeignService;

    @Autowired
    private Producer producer;


    /**
     * 查询用户接口(福包天下管理系统使用)
     *
     * @param jsonObject
     * @return
     */
    public JSONObject queryAuthMember(JSONObject jsonObject) {
        String realName = jsonObject.getString("realName");
        String nickName = jsonObject.getString("nickName");
        String phone = jsonObject.getString("phone");
        Integer status = jsonObject.getInteger("status");
        String limit = jsonObject.getString("limit");
        String page = jsonObject.getString("page");
        if (StringUtil.isNullOrEmpty(limit, page)) {
            throw new BusinessException(ResultCodeEnum.FAIL, "必填参数为空!");
        }
        if (!StringUtil.isNullOrEmpty(realName) && realName.contains("%")) {
            realName = realName.replaceAll("%", "\\\\%");
        }
        if (!StringUtil.isNullOrEmpty(nickName) && nickName.contains("%")) {
            nickName = nickName.replaceAll("%", "\\\\%");
        }
        if (!StringUtil.isNullOrEmpty(phone) && phone.contains("%")) {
            phone = phone.replaceAll("%", "\\\\%");
        }
        AuthMember authMember = new AuthMember();
        authMember.setName(realName);
        authMember.setNickname(nickName);
        authMember.setPhone(phone);
        authMember.setStatus(status);
        PageHelper.startPage(Integer.parseInt(page), Integer.parseInt(limit));
        List<AuthMember> authMemberList = authMemberMapper.queryAuthMember(authMember);
        log.info("查询福包天下用户个数={},数据={}", authMemberList.size(), authMemberList);
        JSONObject resJson = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject json;
        if (!authMemberList.isEmpty()) {
            for (AuthMember auth : authMemberList) {
                json = new JSONObject();
                json.put("userId", auth.getGuid());
                json.put("realName", auth.getName());
                json.put("nickName", auth.getNickname());
                json.put("phone", auth.getPhone());
                json.put("registerTime", auth.getCreatetime());
                json.put("status", auth.getStatus());
                json.put("idCard", auth.getIdcardnumber());
                jsonArray.add(json);
            }
        }
        List<AuthMember> authMemberCount = authMemberMapper.queryAuthMember(authMember);
        resJson.put("data", jsonArray);
        resJson.put("countSum", authMemberCount.size());
        return resJson;
    }

    /**
     * 查询空间主接口(福包天下管理系统使用)
     *
     * @param jsonObject
     * @return
     */
    public JSONObject querySpaceMaster(JSONObject jsonObject) {
        log.info("查询空间主接口入参={}", jsonObject);
        String realName = jsonObject.getString("realName");
        String nickName = jsonObject.getString("nickName");
        String phone = jsonObject.getString("phone");
        Integer status = jsonObject.getInteger("status");
        String limit = jsonObject.getString("limit");
        String page = jsonObject.getString("page");
        if (StringUtil.isNullOrEmpty(limit, page)) {
            throw new BusinessException(ResultCodeEnum.FAIL, "必填参数为空!");
        }
        if (!StringUtil.isNullOrEmpty(realName) && realName.contains("%")) {
            realName = realName.replaceAll("%", "\\\\%");
        }
        if (!StringUtil.isNullOrEmpty(nickName) && nickName.contains("%")) {
            nickName = nickName.replaceAll("%", "\\\\%");
        }
        if (!StringUtil.isNullOrEmpty(phone) && phone.contains("%")) {
            phone = phone.replaceAll("%", "\\\\%");
        }
        AuthSpaceMaster spaceMaster = new AuthSpaceMaster();
        spaceMaster.setRealName(realName);
        spaceMaster.setNickname(nickName);
        spaceMaster.setPhone(phone);
        spaceMaster.setState(status);
        PageHelper.startPage(Integer.parseInt(page), Integer.parseInt(limit));
        List<AuthSpaceMaster> authSpaceMasterList = authSpaceMasterMapper.querySpaceMaster(spaceMaster);
        log.info("查询个数={},数据={}", authSpaceMasterList.size(), authSpaceMasterList);
        JSONArray jsonArray = new JSONArray();
        JSONObject json;
        if (!authSpaceMasterList.isEmpty()) {
            for (AuthSpaceMaster auth : authSpaceMasterList) {
                String num = "";
                try {
                    JSONObject spaceJson = new JSONObject();
                    spaceJson.put("ownerId", auth.getUserId());
                    RWrapper<JSONObject> resData = fbtxFeignService.spaceNum(spaceJson);
                    log.info("获取空间主{}拥有的空间数量返回数据={}", auth.getUserId(), resData);
                    if ("1000".equals(resData.getCode())) {
                        num = resData.getData().getString("count");
                    }
                } catch (Exception e) {
                    log.info("空间主userid={},获取空间主拥有的空间数量异常", auth.getUserId());
                    e.printStackTrace();
                }
                json = new JSONObject();
                json.put("userId", auth.getUserId());
                json.put("realName", auth.getRealName());
                json.put("nickName", auth.getNickname());
                json.put("phone", auth.getPhone());
                json.put("registerTime", auth.getRegisterTime());
                json.put("status", auth.getState());
                json.put("spaceNum", num);
                json.put("idCard", auth.getIdcardNumber());
                json.put("spaceMasterTime", auth.getSpaceMasterTime());
                json.put("title", auth.getTitle());
                jsonArray.add(json);
            }
        }
        List<AuthSpaceMaster> authSpaces = authSpaceMasterMapper.querySpaceMaster(spaceMaster);
        JSONObject resJson = new JSONObject();
        resJson.put("data", jsonArray);
        resJson.put("countSum", authSpaces.size());
        return resJson;
    }

    /**
     * 根据userId查询用户信息(福包天下管理系统使用)
     *
     * @param userId
     * @return
     */
    public JSONObject queryAuthMemberById(String userId) {
        AuthMember authMember = authMemberMapper.selectByPrimaryKey(userId);
        log.info("authMember={}", authMember);
        JSONObject jsonObject = new JSONObject();
        if (authMember != null) {
            jsonObject.put("nickName", authMember.getNickname());
            jsonObject.put("userId", authMember.getId());
            String face = authMember.getFacephotopath();
            if (face != null) {
                if (face.contains("http")) {
                    jsonObject.put("facePhotoPath", face);
                } else {
                    jsonObject.put("facePhotoPath", config.getPicture_url() + face);
                }
            }
            jsonObject.put("phone", authMember.getPhone());
            jsonObject.put("gender", authMember.getGender());
            String tit = "";
            //1:公益使者、2:公益达人、3:公益大使',
            Integer title = authMember.getTitle();
            if (title != null) {
                if (title == 1) {
                    tit = "公益使者";
                }
                if (title == 2) {
                    tit = "公益达人";
                }
                if (title == 3) {
                    tit = "公益大使";
                }
            }
            jsonObject.put("title", tit);
            jsonObject.put("realName", authMember.getName());
            String idCard = authMember.getIdcardnumber();
            jsonObject.put("idCard", idCard);
            if (!StringUtil.isNullOrEmpty(idCard)) {
                jsonObject.put("idCardStatus", "已认证");
            } else {
                jsonObject.put("idCardStatus", "未认证");
            }
            jsonObject.put("weiXinNum", authMember.getWeixinNum());
            jsonObject.put("email", authMember.getEmail());
            jsonObject.put("address", authMember.getAddress());
            Integer sta = authMember.getStatus();
            String status = "";
            if (sta == Integer.parseInt(CommonConfig.STATUS_0)) {
                status = "封号";
            }
            if (sta == Integer.parseInt(CommonConfig.STATUS_1)) {
                status = "生效";
            }
            if (sta == Integer.parseInt(CommonConfig.STATUS_2)) {
                status = "禁言";
            }
            jsonObject.put("status", status);
            jsonObject.put("registerTime", authMember.getCreatetime());
        }
        return jsonObject;
    }

    /**
     * 根据userId查询空间主信息(福包天下管理系统使用)
     *
     * @param userId
     * @return
     */
    public JSONObject querySpaceMasterById(String userId) {
        AuthSpaceMaster spaceMaster = authSpaceMasterMapper.selectByUserId(userId);
        log.info("spaceMaster={}", spaceMaster);
        JSONObject jsonObject = new JSONObject();
        if (spaceMaster != null) {
            jsonObject.put("nickName", spaceMaster.getNickname());
            String face = spaceMaster.getHeadPortrait();
            if (face != null) {
                if (face.contains("http")) {
                    jsonObject.put("facePhotoPath", face);
                } else {
                    jsonObject.put("facePhotoPath", config.getPicture_url() + face);
                }
            }
            jsonObject.put("phone", spaceMaster.getPhone());
            jsonObject.put("gender", spaceMaster.getGender());
            String tit = "";
            //1:公益使者、2:公益达人、3:公益大使',
            Integer title = spaceMaster.getTitle();
            if (title != null) {
                if (title == 1) {
                    tit = "公益使者";
                }
                if (title == 2) {
                    tit = "公益达人";
                }
                if (title == 3) {
                    tit = "公益大使";
                }
            }
            jsonObject.put("title", tit);
            jsonObject.put("realName", spaceMaster.getRealName());
            String idCard = spaceMaster.getIdcardNumber();
            jsonObject.put("idCard", idCard);
            if (!StringUtil.isNullOrEmpty(idCard)) {
                jsonObject.put("idCardStatus", "已认证");
            } else {
                jsonObject.put("idCardStatus", "未认证");
            }
            jsonObject.put("weiXinNum", spaceMaster.getWeixinNum());
            jsonObject.put("email", spaceMaster.getEmail());
            jsonObject.put("address", spaceMaster.getAddress());
            Integer sta = spaceMaster.getState();
            String status = "";
            if (sta == Integer.parseInt(CommonConfig.STATUS_0)) {
                status = "封号";
            }
            if (sta == Integer.parseInt(CommonConfig.STATUS_1)) {
                status = "生效";
            }
            if (sta == Integer.parseInt(CommonConfig.STATUS_2)) {
                status = "禁言";
            }
            jsonObject.put("status", status);
            jsonObject.put("registerTime", spaceMaster.getRegisterTime());
            jsonObject.put("spaceMasterTime", spaceMaster.getSpaceMasterTime());
            String registerChannel = spaceMaster.getRegisterChannel();
            //申请方式 1:APP申请、2:后台新增,3:后台升级',
            if (!StringUtil.isNullOrEmpty(registerChannel)) {
                if (CommonConfig.REGISTER_CHANNEL_1.equals(registerChannel)) {
                    registerChannel = "APP申请";
                }
                if (CommonConfig.REGISTER_CHANNEL_2.equals(registerChannel)) {
                    registerChannel = "后台新增";
                }
                if (CommonConfig.REGISTER_CHANNEL_3.equals(registerChannel)) {
                    registerChannel = "后台升级";
                }
            }
            jsonObject.put("registerChannel", registerChannel);
            String num = "";
            try {
                JSONObject spaceJson = new JSONObject();
                spaceJson.put("ownerId", spaceMaster.getUserId());
                RWrapper<JSONObject> resData = fbtxFeignService.spaceNum(spaceJson);
                log.info("获取空间主{}拥有的空间数量返回数据={}", spaceMaster.getUserId(), resData);
                if ("1000".equals(resData.getCode())) {
                    num = resData.getData().getString("count");
                }
            } catch (Exception e) {
                log.info("空间主userid={},获取空间主拥有的空间数量异常", spaceMaster.getUserId());
                e.printStackTrace();
            }
            jsonObject.put("spaceNum", num);
        }
        return jsonObject;
    }


    /**
     * 用户升级为空间主
     * 1.去验证身份证号和姓名是否匹配
     * 2.去message中改变角色
     * 3.去auth中改变角色
     * 4.去payment中改变角色
     * 5.去被禁记录中改变角色
     * 6.插入空间主表
     * 7.删除用户表
     *
     * @param jsonObject
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void upgrade(JSONObject jsonObject) {
        String userId = jsonObject.getString("userId");
        String realName = jsonObject.getString("realName");
        String idCard = jsonObject.getString("idCard");
        AuthMember authMember = authMemberMapper.selectByPrimaryKey(userId);
        log.info("用户userId={},查询数据={}", userId, authMember);
        if (authMember == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOTEXIST, "用户不存在!");
        }
        //实名认证
        String sex = checkIdCardCheck(userId, idCard, realName);
        //修改message角色
        updateOwner(userId);
        //修改auth角色
        upgradeAuth(jsonObject);
        //修改payment角色
        updateUserType(userId);
        //去被禁记录中改变角色
        updatePenaltyRecord(userId);
        log.info("开始发送mq");
        JSONObject json = new JSONObject();
        json.put("userName", realName);
        json.put("guid", userId);
        log.info("发送mq消息：" + json);
        Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", json.toJSONString().getBytes());
        SendResult send = producer.send(message);
        log.info("mq的消息id：" + send.getMessageId());
        log.info("结束发送mq");

        AuthSpaceMaster authSpaceMaster = new AuthSpaceMaster();
        authSpaceMaster.setRealName(realName);
        authSpaceMaster.setPhone(authMember.getPhone());
        authSpaceMaster.setUserId(userId);
        authSpaceMaster.setGender(sex);
        authSpaceMaster.setNickname(authMember.getNickname());
        authSpaceMaster.setHeadPortrait(authMember.getFacephotopath());
        authSpaceMaster.setIdcardNumber(idCard);
        authSpaceMaster.setWeixinNum(authMember.getWeixinNum());
        authSpaceMaster.setEmail(authMember.getEmail());
        authSpaceMaster.setAddress(authMember.getAddress());
        authSpaceMaster.setRegisterTime(authMember.getCreatetime());
        authSpaceMaster.setRegisterChannel(CommonConfig.REGISTER_CHANNEL_3);
        authSpaceMaster.setSpaceMasterTime(new Date());
        authSpaceMaster.setState(authMember.getStatus());
        authSpaceMaster.setCreateTime(new Date());
        authSpaceMaster.setTitle(CommonConfig.TITLE_1);
        authSpaceMaster.setUnionId(authMember.getUnionId());
        authSpaceMasterMapper.insertSelective(authSpaceMaster);
        log.info("插入空间主表成功。");
        authMemberMapper.deleteByPrimaryKey(userId);
        log.info("删除用户表成功！");
    }

    /**
     * 封号、解封、禁言、解禁  sign：1、2、3、4
     *
     * @param jsonObject
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(JSONObject jsonObject) {
        String userIds = jsonObject.getString("userIds");
        String sign = jsonObject.getString("sign");
        String roleId = jsonObject.getString("roleId");
        if (StringUtil.isNullOrEmpty(userIds, sign, roleId)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "缺少必填参数!");
        }
        String[] user = userIds.split(",");
        for (String userId : user) {
            //用户
            if (roleId.equals(CommonConfig.FBTX_ROLE_ID)) {
                if ("1".equals(sign)) {
                    updateAuthStatus(userId, sign);
                    addPenaltyRecord(userId, 0, Integer.parseInt(CommonConfig.FBTX_ROLE_ID));
                    authMemberMapper.updateStatus(userId, 0);
                }
                if ("2".equals(sign)) {
                    //解封只是解封被封号的人  先查询用户是不是封号状态 0封号状态
                    AuthMember authMember = authMemberMapper.selectByguid(userId);
                    if (authMember != null) {
                        if (authMember.getStatus() != null && authMember.getStatus() == 0) {
                            updateAuthStatus(userId, sign);
                            authMemberMapper.updateStatus(userId, 1);
                        }
                    }
                }

                if ("3".equals(sign)) {
                    //禁言只是正常状态的被禁言  1：正常状态
                    AuthMember authMember = authMemberMapper.selectByguid(userId);
                    if (authMember != null) {
                        if (authMember.getStatus() != null && authMember.getStatus() == 1) {
                            updateAuthStatus(userId, sign);
                            addPenaltyRecord(userId, 2, Integer.parseInt(CommonConfig.FBTX_ROLE_ID));
                            authMemberMapper.updateStatus(userId, 2);
                        }
                    }
                }

                if ("4".equals(sign)) {
                    //解禁 只是被禁言的被解禁 2：禁言
                    AuthMember authMember = authMemberMapper.selectByguid(userId);
                    if (authMember != null) {
                        if (authMember.getStatus() != null && authMember.getStatus() == 2) {
                            updateAuthStatus(userId, sign);
                            authMemberMapper.updateStatus(userId, 1);
                        }
                    }
                }
            }
            //空间主
            if (roleId.equals(CommonConfig.FBTX_SPACE_ROLE_ID)) {
                if ("1".equals(sign)) {
                    updateAuthStatus(userId, sign);
                    addPenaltyRecord(userId, 0, Integer.parseInt(CommonConfig.FBTX_SPACE_ROLE_ID));
                    authSpaceMasterMapper.updateStatus(userId, 0);
                }
                if ("2".equals(sign)) {
                    //解封只是解封被封号的人  先查询用户是不是封号状态 0封号状态
                    AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByUserId(userId);
                    if (authSpaceMaster != null) {
                        if (authSpaceMaster.getState() != null && authSpaceMaster.getState() == 0) {
                            updateAuthStatus(userId, sign);
                            authSpaceMasterMapper.updateStatus(userId, 1);
                        }
                    }
                }

                if ("3".equals(sign)) {
                    AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByUserId(userId);
                    if (authSpaceMaster != null) {
                        if (authSpaceMaster.getState() != null && authSpaceMaster.getState() == 1) {
                            updateAuthStatus(userId, sign);
                            addPenaltyRecord(userId, 2, Integer.parseInt(CommonConfig.FBTX_SPACE_ROLE_ID));
                            authSpaceMasterMapper.updateStatus(userId, 2);
                        }
                    }
                }
                if ("4".equals(sign)) {
                    AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByUserId(userId);
                    if (authSpaceMaster != null) {
                        if (authSpaceMaster.getState() != null && authSpaceMaster.getState() == 2) {
                            updateAuthStatus(userId, sign);
                            authSpaceMasterMapper.updateStatus(userId, 1);
                        }
                    }
                }
            }
        }
        log.info("全部修改完成！");
    }


    /**
     * 修改auth中状态
     */
    public void updateAuthStatus(String userId, String sign) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        if ("1".equals(sign)) {
            jsonObject.put("status", "0");
        }
        if ("2".equals(sign)) {
            jsonObject.put("status", "1");
        }
        if ("3".equals(sign)) {
            jsonObject.put("status", "2");
        }
        if ("4".equals(sign)) {
            jsonObject.put("status", "1");
        }
        RWrapper res = authFeignService.updateStatus(jsonObject);
        log.info("调用auth返回={}", res);
        if (!"1000".equals(res.getCode())) {
            throw new BusinessException(ResultCodeEnum.FAIL, "修改失败!");
        }
    }

    /**
     * 新增空间主
     *
     * @param jsonObject
     */
    public void addSpaceMaster(JSONObject jsonObject) {
        String phone = jsonObject.getString("phone");
        String realName = jsonObject.getString("realName");
        String idCard = jsonObject.getString("idCard");
        if (StringUtil.isNullOrEmpty(phone, realName, idCard)) {
            throw new BusinessException(ResultCodeEnum.FAIL, "请补全信息!");
        }
        AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByPhone(phone);
        log.info("空间主authSpaceMaster={}", authSpaceMaster);
        if (authSpaceMaster != null) {
            log.info("手机号phone={},已经是空间主!", phone);
            throw new BusinessException(ResultCodeEnum.FAIL, "该用户已经是空间主!");
        }
        AuthMember authMember = authMemberMapper.getAuthMemberByPhoneAndRole(phone, Integer.parseInt(CommonConfig.FBTX_ROLE_ID));
        log.info("普通用户authMember={}", authMember);
        if (authMember != null) {
            log.info("手机号phone={},已经是福包天下用户!", phone);
            throw new BusinessException(ResultCodeEnum.FAIL, "该用户是福包天下用户，需要升级才可以成为空间主!");
        }

        log.info("开始验证身份证和姓名是否匹配-------------------");
        JSONObject authJSon = new JSONObject();
        authJSon.put("userId", UUID.randomUUID().toString().replace("-", ""));
        authJSon.put("userType", "2");
        authJSon.put("idCardNum", idCard);
        authJSon.put("realName", realName);
        RWrapper resData = authFeignService.checkIdCardCheck(authJSon);
        log.info("返回数据={}", resData);
        if (!"1000".equals(resData.getCode())) {
            throw new BusinessException(ResultCodeEnum.FAIL, resData.getMsg());
            //throw new BusinessException(ResultCodeEnum.FAIL, "身份证号和姓名不匹配!");
        }
        JSONObject resJson = JSONObject.parseObject(JSONObject.toJSONString(resData.getData()));
        String sex = resJson.getString("sex");
        log.info("结束验证身份证和姓名是否匹配-------------------");
        log.info("开始去gts注册---------------------------------");
        JSONObject gtsJson = new JSONObject();
        gtsJson.put("platform", CommonConfig.USERNAME_PREFIX);
        gtsJson.put("phone", phone);
        gtsJson.put("password", "123456");
        gtsJson.put("roleid", CommonConfig.FBTX_SPACE_ROLE_ID);
        gtsJson.put("userType", CommonConfig.FBTX_SPACE_ROLE_ID);
        gtsJson.put("sex", sex);
        gtsJson.put("realName", realName);
        String front = phone.substring(0, 3);
        String alert = phone.substring(phone.length() - 2);
        String nickName = front + "******" + alert;
        gtsJson.put("nickName", nickName);
        gtsJson.put("idCard", idCard);
        log.info("注册入参={}", gtsJson);
        RWrapper<JSONObject> resRegister = gtsFeignService.register(gtsJson);
        log.info("用户注册返回数据={}", resRegister);
        if (!"1000".equals(resRegister.getCode())) {
            throw new BusinessException(ResultCodeEnum.FAIL, "添加失败!");
        }
        log.info("结束去gts注册---------------------------------");
    }

    /**
     * 被罚记录表中插入数据
     *
     * @param userId
     * @param type
     */
    public void addPenaltyRecord(String userId, Integer type, Integer roleId) {
        log.info("被罚记录表中插入数据userId={},type={}", userId, type);
        PenaltyRecord penaltyRecord = new PenaltyRecord();
        penaltyRecord.setUserId(userId);
        penaltyRecord.setType(type);
        penaltyRecord.setRoleId(roleId);
        penaltyRecord.setCreateTime(new Date());
        penaltyRecordMapper.insert(penaltyRecord);
        log.info("插入成功！");
    }

    /**
     * 用户的被罚记录
     *
     * @param jsonObject
     */
    public JSONObject userPenaltyRecord(JSONObject jsonObject) {
        String realName = jsonObject.getString("realName");
        String nickName = jsonObject.getString("nickName");
        String phone = jsonObject.getString("phone");
        String roleId = jsonObject.getString("roleId");
        if (StringUtil.isNullOrEmpty(roleId)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "缺少必填参数!");
        }
        if (!StringUtil.isNullOrEmpty(realName) && realName.contains("%")) {
            realName = realName.replaceAll("%", "\\\\%");
        }
        if (!StringUtil.isNullOrEmpty(nickName) && nickName.contains("%")) {
            nickName = nickName.replaceAll("%", "\\\\%");
        }
        if (!StringUtil.isNullOrEmpty(phone) && phone.contains("%")) {
            phone = phone.replaceAll("%", "\\\\%");
        }
        String limit = jsonObject.getString("limit");
        String page = jsonObject.getString("page");
        List<PenaltyRecordVo> penaltyRecordVos = new ArrayList<>();
        if (CommonConfig.FBTX_ROLE_ID.equals(roleId)) {
            PageHelper.startPage(Integer.parseInt(page), Integer.parseInt(limit));
            penaltyRecordVos = penaltyRecordMapper.selectPage(realName, nickName, phone, Integer.parseInt(roleId));
        }
        if (CommonConfig.FBTX_SPACE_ROLE_ID.equals(roleId)) {
            PageHelper.startPage(Integer.parseInt(page), Integer.parseInt(limit));
            penaltyRecordVos = penaltyRecordMapper.selectSpacePage(realName, nickName, phone, Integer.parseInt(roleId));
        }

        log.info("penaltyRecordVos个数={}", penaltyRecordVos.size());
        JSONArray jsonArray = new JSONArray();
        JSONObject json;
        if (!penaltyRecordVos.isEmpty()) {
            for (PenaltyRecordVo penaltyRecordVo : penaltyRecordVos) {
                json = new JSONObject();
                json.put("userId", penaltyRecordVo.getUserId());
                json.put("realName", penaltyRecordVo.getRealName());
                json.put("nickName", penaltyRecordVo.getNickName());
                json.put("phone", penaltyRecordVo.getPhone());
                json.put("createTime", penaltyRecordVo.getCreateTime());
                json.put("type", penaltyRecordVo.getType());
                json.put("status", penaltyRecordVo.getStatus());
                jsonArray.add(json);
            }
        }

        JSONObject resJson = new JSONObject();
        List<PenaltyRecordVo> penalty = new ArrayList<>();

        if (CommonConfig.FBTX_ROLE_ID.equals(roleId)) {
            penalty = penaltyRecordMapper.selectPage(realName, nickName, phone, Integer.parseInt(roleId));
        }
        if (CommonConfig.FBTX_SPACE_ROLE_ID.equals(roleId)) {
            penalty = penaltyRecordMapper.selectSpacePage(realName, nickName, phone, Integer.parseInt(roleId));
        }
        resJson.put("data", jsonArray);
        resJson.put("countSum", penalty.size());
        return resJson;
    }

    /**
     * 验证实名认证
     *
     * @param userId
     * @param idCard
     * @param realName
     * @return
     */
    public String checkIdCardCheck(String userId, String idCard, String realName) {
        log.info("开始验证身份证和姓名是否匹配");
        JSONObject authJSon = new JSONObject();
        authJSon.put("userId", userId);
        authJSon.put("userType", "2");
        authJSon.put("idCardNum", idCard);
        authJSon.put("realName", realName);
        RWrapper resData = authFeignService.checkIdCardCheck(authJSon);
        log.info("返回数据={}", resData);
        if (!"1000".equals(resData.getCode())) {
            throw new BusinessException(ResultCodeEnum.FAIL, resData.getMsg());
        }
        JSONObject resJson = JSONObject.parseObject(JSONObject.toJSONString(resData.getData()));
        String sex = resJson.getString("sex");
        log.info("结束验证身份证和姓名是否匹配sex={}", sex);
        return sex;
    }

    /**
     * 修改message中用户状态
     *
     * @param userId
     */
    public void updateOwner(String userId) {
        log.info("开始调用message变为空间主");
        JSONObject messJson = new JSONObject();
        messJson.put("userId", userId);
        RWrapper resMess = messageFeignService.updateOwner(messJson);
        log.info("resMess={}", resMess);
        if (!"1000".equals(resMess.getCode())) {
            throw new BusinessException(ResultCodeEnum.FAIL, "升级失败！");
        }
        log.info("结束调用message变为空间主");
    }

    /**
     * 去auth修改角色
     *
     * @param jsonObject
     */
    public void upgradeAuth(JSONObject jsonObject) {
        log.info("开始调用auth，去改变角色");
        RWrapper resDat = authFeignService.upgrade(jsonObject);
        log.info("resDat={}", resDat);
        if (!"1000".equals(resDat.getCode())) {
            throw new BusinessException(ResultCodeEnum.FAIL, "升级失败!");
        }
        log.info("结束调用auth，去改变角色");
    }

    /**
     * 去payment修改角色
     *
     * @param userId
     */
    public void updateUserType(String userId) {
        log.info("开始调用payment改变角色");
        JSONObject payJson = new JSONObject();
        payJson.put("userId", userId);
        payJson.put("roleId", CommonConfig.FBTX_SPACE_ROLE_ID);
        log.info("payJson={}", payJson);
        RWrapper pay = paymentFeignService.updateUserType(payJson);
        log.info("调用payment返回数据={}", pay);
        if (!"1000".equals(pay.getCode())) {
            throw new BusinessException(ResultCodeEnum.FAIL, "升级失败!");
        }
        log.info("结束调用payment改变角色");
    }

    /**
     * 修改被禁记录
     *
     * @param userId
     */
    public void updatePenaltyRecord(String userId) {
        log.info("查看用户是否有被禁记录-----------");
        List<PenaltyRecord> penaltyRecords = penaltyRecordMapper.selectByUserId(userId);
        log.info("penaltyRecords={}", penaltyRecords);
        if (!penaltyRecords.isEmpty()) {
            for (PenaltyRecord penaltyRecord : penaltyRecords) {
                penaltyRecord.setRoleId(Integer.parseInt(CommonConfig.FBTX_SPACE_ROLE_ID));
                penaltyRecord.setUpdateTime(new Date());
                penaltyRecordMapper.updateByPrimaryKeySelective(penaltyRecord);
                log.info("更改角色完成");
            }
        }
        log.info("结束用户是否有被禁记录-----------");
    }
}
