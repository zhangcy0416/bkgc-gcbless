package com.bkgc.game.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Producer;
import com.bkgc.bean.game.RewardFactor;
import com.bkgc.bean.pay.Account;
import com.bkgc.bean.user.AuthMember;
import com.bkgc.bean.user.AuthSpaceMaster;
import com.bkgc.bless.config.Config;
import com.bkgc.bless.config.SignClient;
import com.bkgc.bless.config.WxConfig;
import com.bkgc.bless.constant.CommonConfig;
import com.bkgc.bless.consumer.AuthFeignService;
import com.bkgc.bless.consumer.GtsFeignService;
import com.bkgc.bless.consumer.PaymentFeignService;
import com.bkgc.bless.mapper.AuthMemberMapper;
import com.bkgc.bless.mapper.AuthSpaceMasterMapper;
import com.bkgc.bless.service.impl.AuthService;
import com.bkgc.bless.service.impl.MessageConsumer;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.*;
import com.bkgc.game.mapper.RewardFactorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class WxService {

    @Autowired
    private AuthMemberMapper authMemberMapper;

    @Resource
    WxConfig wxConfig;

    @Resource
    private Config config;

    @Autowired
    private Producer producer;

    @Autowired
    RewardFactorMapper rewardFactorMapper;

    @Autowired
    private PaymentFeignService paymentFeignService;

    @Autowired
    private AuthFeignService authFeignService;

    @Resource
    private SignClient signClient;


    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MessageConsumer messageConsumer;

    @Autowired
    private GtsFeignService gtsFeignService;

    @Autowired
    private AuthSpaceMasterMapper authSpaceMasterMapper;

    @Autowired
    private AuthService authService;


    @SuppressWarnings("unchecked")
    public JSONObject wxLogin(String code) {
        JSONObject data = new JSONObject();
        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("appid", wxConfig.getAppid());
        tokenParams.put("secret", wxConfig.getSecret());
        tokenParams.put("code", code);
        tokenParams.put("grant_type", wxConfig.getTokenGrantType());

        JSONObject tokenInfo = JSONObject.parseObject(HttpRequest.sendPost(wxConfig.getWxGetToken(), tokenParams));
        log.info("微信接口返回信息=" + tokenInfo);
        Object object = tokenInfo.get("access_token");
        if (object == null) {
            data.put("code", "40163");
            data.put("msg", "请重新扫码");
            return data;
        }
        String openid = (String) tokenInfo.get("openid");
        String access_token = (String) tokenInfo.get("access_token");
        String lang = wxConfig.getLang();
        Map<String, String> getUserParams = new HashMap<>();
        getUserParams.put("access_token", access_token);
        getUserParams.put("openid", openid);
        getUserParams.put("lang", lang);

        JSONObject sendGet = HttpRequest.sendGet(wxConfig.getGetInfoUrl(), getUserParams);
        log.info("微信接口返回unionId的具体信息=" + sendGet);
        String unionid = (String) sendGet.get("unionid");

        /*Map map = new HashMap<>();
        map.put("unionId", unionid);
        map.put("client_secret", config.getBviClientSecert());
        map.put("client_id", config.getBviClientId());*/

        //String res = authFeignService.getByUnionId(JSONObject.parseObject(JSON.toJSONString(map))).toString();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("unionId", unionid);
        log.info("开始访问4A登录");
        RWrapper<Map<String, String>> res = packData(jsonObject);
        log.info("结束访问4A登录  返回数据res={}", res.toString());
        JSONObject result = JSONObject.parseObject(res.toString());
        if (!"1000".equals(result.get("code").toString())) {
            result.put("data", sendGet);
            return result;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public JSONObject wxNewLogin(String code) {
        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("appid", wxConfig.getAppid());
        tokenParams.put("secret", wxConfig.getSecret());
        tokenParams.put("code", code);
        tokenParams.put("grant_type", wxConfig.getTokenGrantType());

        JSONObject tokenInfo = JSONObject.parseObject(HttpRequest.sendPost(wxConfig.getWxGetToken(), tokenParams));
        log.info("微信获取token接口返回信息={}", tokenInfo);
        if (null == tokenInfo) {
            throw new BusinessException(ResultCodeEnum.FAIL, "微信获取token接口返回数据异常!");
        }
        String openId = tokenInfo.getString("openid");
        String accessToken = tokenInfo.getString("access_token");
        String lang = wxConfig.getLang();
        log.info("openId={},access_token={},lang={}", openId, accessToken, lang);
        if (StringUtil.isNullOrEmpty(openId, accessToken, lang)) {
            throw new BusinessException(ResultCodeEnum.FAIL, "微信获取token接口返回数据异常!");
        }
        Map<String, String> getUserParams = new HashMap<>();
        getUserParams.put("access_token", accessToken);
        getUserParams.put("openid", openId);
        getUserParams.put("lang", lang);

        JSONObject sendGet = HttpRequest.sendGet(wxConfig.getGetInfoUrl(), getUserParams);
        log.info("微信获取个人信息接口返回具体信息={}", sendGet);
        if (null == sendGet) {
            throw new BusinessException(ResultCodeEnum.FAIL, "微信获取个人信息接口返回数据异常!");
        }

        String unionId = sendGet.getString("unionid");
        AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByUnionId(unionId);
        log.info("通过unionId查询空间主={}", authSpaceMaster);
        JSONObject result = null;
        if (null == authSpaceMaster) {
            //福包天下用户
            AuthMember authMember = authMemberMapper.getAuthMemberByUnionIdAndRole(unionId, Integer.parseInt(CommonConfig.FBTX_ROLE_ID));
            log.info("通过unionId查询福包天下用户={}", authMember);
            if (null == authMember) {
                JSONObject json = new JSONObject();
                json.put("code", "2016");
                json.put("msg", "用户不存在");
                json.put("data", sendGet);
                return json;
            } else {
                //普通用户登录
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("unionId", unionId);
                jsonObject.put("userRoleId", CommonConfig.FBTX_ROLE_ID);
                RWrapper<Map<String, String>> res = packData(jsonObject);
                log.info("用户登录返回数据={}", res);
                result = JSONObject.parseObject(res.toString());
                log.info("用户登录返回数据={}", result);
                if (!"1000".equals(result.getString("code"))) {
                    result.put("data", sendGet);
                    return result;
                }
            }
        } else {
            //空间主登录
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("unionId", unionId);
            jsonObject.put("userRoleId", CommonConfig.FBTX_SPACE_ROLE_ID);
            RWrapper<Map<String, String>> res = packData(jsonObject);
            log.info("空间主登录返回数据={}", res);
            result = JSONObject.parseObject(res.toString());
            log.info("空间主登录返回数据={}", result);
            if (!"1000".equals(result.getString("code"))) {
                result.put("data", sendGet);
                return result;
            }
        }
        return result;
    }


    public String getInfo4Game(String guid) {
        JSONObject getUserParams = new JSONObject();
        getUserParams.put("userId", guid);
        getUserParams.put("clientID", signClient.getClientId());
        getUserParams.put("nonce_str", UUID.randomUUID().toString());

        String sign = Signature.getSign(getUserParams, signClient.getClientSecret());
        getUserParams.put("sign", sign);

        JSONObject result = paymentFeignService.getAccountInfo(getUserParams).getData();
        if (!"1000".equals(result.get("code").toString())) {
            return result.toString();
        }
        log.info("账户信息={}", result.getString("data"));
        //Account account = JSON.parseObject(result.getString("data"), Account.class);
        JSONObject dataJson = result.getJSONObject("data");
        Account account = new Account();
        account.setAccountBalance(dataJson.getDouble("accountbalance"));
        account.setBlessAmount(dataJson.getDouble("blessamount"));

        JSONObject data = new JSONObject();
        data.put("userAccountInfo", account);//用户账户余额
        RewardFactor r = rewardFactorMapper.selectByPrimaryKey(guid);
        if (r == null) {
            data.put("luckyValue", 0);
        } else {
            data.put("luckyValue", r.getLuckyValue());
        }
        JSONObject resultData = new JSONObject();
        resultData.put("code", 1000);
        resultData.put("msg", "获取数据成功");
        resultData.put("data", data);
        return resultData.toString();
    }

    /**
     * 检验该用户是不是福包用户  如果不是入注册
     *
     * @param param
     * @return
     */
    public String register(JSONObject param) {
        log.info("微信注册 接口请求参数：{}", param);
        String phone = param.getString("phone");
        String openId = param.getString("openId");
        String smgCode = param.getString("smgCode");
        String unionId = param.getString("unionId");
        String clientID = param.getString("clientID");
        log.info("开始验证验证码是否正确！");
        RWrapper r = messageConsumer.verify(phone, smgCode);
        log.info("结束验证验证码是否正确！，返回数据={}", r);
        if (!"1000".equals(r.getCode())) {
            return r.toString();
        }
        AuthMember authMember = null;
        log.info("开始通过手机号去AuthMember查询数据，phone={}", phone);
        if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
            log.info("通过手机号查询空间主");
            AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByPhone(phone);
            log.info("authSpaceMaster={}", authSpaceMaster);
            if (authSpaceMaster == null) {
                authMember = authMemberMapper.getAuthMemberByPhoneAndRole(phone, Integer.parseInt(CommonConfig.FBTX_ROLE_ID));
                log.info("authMember={}", authMember);
                param.put("userRoleId", CommonConfig.FBTX_ROLE_ID);
            } else {
                JSONObject json = new JSONObject();
                json.put("guid", authSpaceMaster.getUserId());
                json.put("unionId", unionId);
                log.info("说明该用户是福包天下空间主，这是第一次绑定微信，开始去绑定guid和unionId");
                RWrapper<JSONObject> result = authFeignService.bindUnionId(json);
                log.info("结束去绑定guid和unionId 返回数据={}", result.toString());
                if (!"1000".equals(result.getCode())) {
                    return result.toString();
                }
                authSpaceMaster.setUnionId(unionId);
                int res = authSpaceMasterMapper.updateByPrimaryKeySelective(authSpaceMaster);
                log.info("res={}", res);
                param.put("userRoleId", CommonConfig.FBTX_SPACE_ROLE_ID);
                RWrapper<Map<String, String>> results = packData(param);
                log.info("微信调用auth（地址：/login/unionId）返回数据results={}", results.toString());
                JSONObject resData = JSONObject.parseObject(results.toString());
                String code = resData.getString("code");
                if ("1000".equals(code)) {
                    JSONObject jsonObj = authService.getRongCloudToken(authSpaceMaster.getUserId(), authSpaceMaster.getNickname(), authSpaceMaster.getHeadPortrait(), CommonConfig.FBTX_SPACE_ROLE_ID);
                    log.info("调用融云={}", jsonObj);

                    JSONObject data = resData.getJSONObject("data");
                    data.put("phone", authSpaceMaster.getPhone());
                    data.put("email", authSpaceMaster.getEmail());
                    data.put("gender", authSpaceMaster.getGender());
                    data.put("name", authSpaceMaster.getRealName());
                    data.put("nickName", authSpaceMaster.getNickname());
                    data.put("idCardNumber", authSpaceMaster.getIdcardNumber());
                    data.put("rongCloudToken", jsonObj.getString("token"));
                    data.put("rongCloudId", jsonObj.getString("id"));
                    return WrapperUtil.ok(data).toString();
                }
                return results.toString();
            }
        } else {
            authMember = authMemberMapper.getNewAuthMemberByPhone(phone);
            log.info("authMember={}", authMember);
            param.put("userRoleId", CommonConfig.GCFB_ROLE_ID);
        }

        log.info("结束通过手机号去AuthMember查询数据 查询authmember={}", authMember);
        if (authMember != null && StringUtil.isNullOrEmpty(authMember.getUnionId())) {
            JSONObject params = new JSONObject();
            params.put("guid", authMember.getId());
            params.put("unionId", unionId);
            log.info("说明该用户是福包用户，这是第一次绑定微信，开始去绑定guid和unionId");
            RWrapper<JSONObject> result = authFeignService.bindUnionId(params);
            log.info("结束去绑定guid和unionId 返回数据={}", result.toString());
            if (!"1000".equals(result.getCode())) {
                return result.toString();
            }
            authMember.setUnionId(unionId);
            authMember.setWeixinopenid(openId);
            log.info("开始更新Auth_Member表中数据！");
            authMemberMapper.updateByPrimaryKeySelective(authMember);
            log.info("成功更新Auth_Member表中数据！");
            RWrapper<Map<String, String>> results = packData(param);
            log.info("微信调用auth（地址：/login/unionId）返回数据results={}", results.toString());
            JSONObject resData = JSONObject.parseObject(results.toString());
            String code = resData.getString("code");
            if ("1000".equals(code)) {
                JSONObject json = null;
                if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                    json = authService.getRongCloudToken(authMember.getId(), authMember.getNickname(), authMember.getFacephotopath(), CommonConfig.FBTX_ROLE_ID);
                    log.info("调用融云={}", json);
                }
                JSONObject data = resData.getJSONObject("data");
                data.put("phone", authMember.getPhone());
                data.put("email", authMember.getEmail());
                data.put("gender", authMember.getGender());
                data.put("name", authMember.getName());
                data.put("nickName", authMember.getNickname());
                data.put("idCardNumber", authMember.getIdcardnumber());
                if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                    data.put("rongCloudToken", json.getString("token"));
                    data.put("rongCloudId", json.getString("id"));
                }
                return WrapperUtil.ok(data).toString();
            }
            return results.toString();
        } else {
            return registerMethod(param);
        }
    }


    /**
     * 微信登录   用户不存在 注册方法
     *
     * @param param
     * @return
     */
    public String registerMethod(JSONObject param) {
        log.info("微信真正注册 接口请求参数：{}", param);
        String phone = param.getString("phone");
        String unionId = param.getString("unionId");
        String iconUrl = param.getString("iconUrl");
        String sex = param.getString("sex");
        String openId = param.getString("openId");
        String backup = param.getString("backup");
        String nickName = param.getString("nickName");
        String clientID = param.getString("clientID");
        backup = (backup == null) ? "" : backup.replaceAll("[^\\u0000-\\uFFFF]", "*");
        nickName = (nickName == null) ? "" : nickName.replaceAll("[^\\u0000-\\uFFFF]", "*");

        //4A注册流程
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("unionId", unionId);
        jsonObject.put("phone", phone);
        jsonObject.put("iconUrl", iconUrl);
        jsonObject.put("backup", backup);

        jsonObject.put("nickName", nickName);
        jsonObject.put("openId", openId);
        jsonObject.put("sex", sex);

        //payment中需要
        jsonObject.put("userType", config.getUserType());

        if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
            jsonObject.put("platform", CommonConfig.USERNAME_PREFIX);
            jsonObject.put("password", "123456");
            jsonObject.put("roleid", CommonConfig.FBTX_ROLE_ID);
        } else {
            jsonObject.put("platform", config.getPlatform());
            jsonObject.put("password", "123456");
            jsonObject.put("roleid", config.getRoleId());
        }


        log.info("微信注册 访问4A 入参={}", jsonObject);
        //RWrapper<JSONObject> result = authFeignService.registerWithUnionId(jsonObject);
        RWrapper<JSONObject> result = gtsFeignService.register(jsonObject);
        log.info("微信注册 访问4A返回数据={}", result.toString());
        if (!"1000".equals(result.getCode())) {
            return result.toString();
        }
        param.put("userRoleId", CommonConfig.FBTX_ROLE_ID);
        RWrapper<Map<String, String>> results = packData(param);
        log.info("返回数据responseData={}", results.toString());
        JSONObject resData = JSONObject.parseObject(results.toString());
        String code = resData.getString("code");
        if ("1000".equals(code)) {
            JSONObject jsonObj = resData.getJSONObject("data");
            String guid = jsonObj.getString("guid");
            AuthMember authMember = authMemberMapper.selectByguid(guid);
            log.info("authMember={}", authMember);
            if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                JSONObject json = authService.getRongCloudToken(authMember.getId(), authMember.getNickname(), authMember.getFacephotopath(), CommonConfig.FBTX_ROLE_ID);
                log.info("调用融云={}", json);
                jsonObj.put("rongCloudToken", json.getString("token"));
                jsonObj.put("rongCloudId", json.getString("id"));
            }
            jsonObj.put("phone", phone);
            jsonObj.put("nickName", nickName);
            return WrapperUtil.ok(jsonObj).toString();
        }
        return results.toString();

        /*
        log.info("手机号" + phone + "在4A上创建成功，开始创建账户");
        JSONObject data = result.getData();
        log.info("手机号" + phone + "在4A上创建成功，开始创建账户 data={}", data);
        String guid = data.getString("guid");
        //payment建立账户
        JSONObject insertParams = new JSONObject();
        insertParams.put("userId", guid);
        insertParams.put("userType", config.getUserType());
        insertParams.put("clientID", signClient.getClientId());
        insertParams.put("nonce_str", UUID.randomUUID().toString());
        String sign = Signature.getSign(insertParams, signClient.getClientSecret());
        insertParams.put("sign", sign);
        log.info("创建账户接口，请求参数为：" + insertParams);
        RWrapper<JSONObject> insertResult = paymentFeignService.insertAuthAccount(insertParams);
        log.info("创建账户接口 返回数据={}", insertResult);
        if (!"1000".equals(insertResult.getCode())) {
            //4A上回滚用户
            log.info("账户创建不成功，对4A平台上的【" + data.getString("username") + "】用户进行逻辑删除！");
            JSONObject delJson = new JSONObject();
            delJson.put("username", data.getString("username"));
            RWrapper delWrap = authFeignService.deleteByUserName(delJson);
            *//*if (!"1000".equals(delWrap.getCode())) {
                log.info("4A删除不成功，再次进行逻辑删除！");
                authFeignService.deleteByUserName(delJson);
            }*//*
            JSONObject returnData = new JSONObject();
            returnData.put("code", "2003");
            returnData.put("msg", "用户注册失败");
            return returnData.toString();
        }

        //在auth_member 添加一条记录
        AuthMember member = new AuthMember();
        member.setId(guid);
        member.setGuid(guid);
        member.setCreatetime(DateTimeUtils.getCurrentDateTime());
        member.setPhone(phone);
        if (StringUtil.isNullOrEmpty(nickName)) {
            member.setNickname(phone);
        } else {
            member.setNickname(nickName);
        }

        if (!StringUtil.isNullOrEmpty(openId)) {
            member.setWeixinopenid(openId);
        }
        member.setFacephotopath(iconUrl);
        member.setGender(sex);
        member.setUnionId(unionId);
        log.info("微信注册 头像地址：{}", member.getFacephotopath());
        int count = authMemberMapper.insertSelective(member);
        if (count != 1) {
            JSONObject delJson = new JSONObject();
            delJson.put("username", data.getString("username"));
            RWrapper delWrap = authFeignService.deleteByUserName(delJson);
            *//*if (!"1000".equals(delWrap.getCode())) {
                log.info("4A删除不成功，再次进行逻辑删除！");
                authFeignService.deleteByUserName(delJson);
            }*//*
            JSONObject returnData = new JSONObject();
            returnData.put("code", "2003");
            returnData.put("msg", "用户注册失败");
            return returnData.toString();
        }

        log.info("发送mq消息");

        JSONObject mqData = new JSONObject();
        mqData.put("guid", guid);
        mqData.put("nickName", nickName);
        mqData.put("phone", phone);
        log.info("发送mq消息：{}", mqData);
        Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", mqData.toJSONString().getBytes());
        SendResult send = producer.send(message);
        log.info("mq的消息id：" + send.getMessageId());
        RWrapper<Map<String, String>> results = packData(param);
        log.info("返回数据responseData={}", results.toString());
        return results.toString();*/
    }

    /**
     * 登录方法
     *
     * @param params
     * @return
     */
    public String loginByApp(JSONObject params) {
        log.info("进入第三方登录，参数为：{}", params);
        String unionId = params.getString("unionId");
        String phone = params.getString("phone");
        String clientID = params.getString("clientID");

        //如果手机号不为空，则跳转到注册方法
        if (!StringUtil.isNullOrEmpty(phone)) {
            log.info("手机号存在，开始走注册流程");
            return register(params);
        }
        AuthMember member;
        if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
            log.info("福包天下app微信登录");
            log.info("首先通过unionId去空间主表中查找用户");
            AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByUnionId(unionId);
            log.info("查询空间主authSpaceMaster={}", authSpaceMaster);
            if (authSpaceMaster == null) {
                log.info("该用户不是空间主查询用户表");
                member = authMemberMapper.getAuthMemberByUnionIdAndRole(unionId, Integer.parseInt(CommonConfig.FBTX_ROLE_ID));
                log.info("该用户不是空间主查询用户表member={}", member);
                params.put("userRoleId", CommonConfig.FBTX_ROLE_ID);
            } else {
                if (authSpaceMaster.getState().intValue() == 0) {
                    log.info("空间主被禁用！");
                    throw new BusinessException(ResultCodeEnum.FAIL, "账号被禁用");
                }
                params.put("userRoleId", CommonConfig.FBTX_SPACE_ROLE_ID);
                log.info("开始请求auth去登录，(地址：/login/unionId)");
                RWrapper<Map<String, String>> results = packData(params);
                log.info("结束请求auth去登录，返回数据={}", results);
                //登录失败，返回错误编码以及错误描述
                if (!"1000".equals(results.getCode())) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", results.getCode());
                    jsonObject.put("msg", results.getMsg());
                    return jsonObject.toString();
                }
                JSONObject json = authService.getRongCloudToken(authSpaceMaster.getUserId(), authSpaceMaster.getNickname(), authSpaceMaster.getHeadPortrait(), CommonConfig.FBTX_SPACE_ROLE_ID);
                log.info("调用融云={}", json);

                JSONObject data = JSONObject.parseObject(JSON.toJSONString(results.getData()));
                data.put("phone", authSpaceMaster.getPhone());
                data.put("guid", authSpaceMaster.getUserId());
                data.put("email", authSpaceMaster.getEmail());
                String headPortrait = authSpaceMaster.getHeadPortrait();
                if (!headPortrait.contains("http")) {
                    headPortrait = config.getPicture_url() + headPortrait;
                }
                data.put("facephotopath", headPortrait);
                data.put("gender", authSpaceMaster.getGender());
                data.put("name", authSpaceMaster.getRealName());
                data.put("nickName", authSpaceMaster.getNickname());
                data.put("idCardNumber", authSpaceMaster.getIdcardNumber());
                data.put("rongCloudToken", json.getString("token"));
                data.put("rongCloudId", json.getString("id"));
                log.info("微信登录成功，返回前台参数为：{}", data);
                return WrapperUtil.ok(data).toString();
            }
        } else {
            log.info("国彩福包app微信登录");
            member = authMemberMapper.getNewAuthMemberByUnionId(unionId);
            params.put("userRoleId", CommonConfig.GCFB_ROLE_ID);
        }


        log.info("通过unionId查询到的AuthMember={}", member);
        //注册  前台通过41031状态码 判断是否需要去注册
        if (member == null) {
            throw new BusinessException(ResultCodeEnum.ERR_41031, "您还不是福包用户，请先注册");
        }
        if (member.getStatus().intValue() == 0) {
            log.info("该账号被禁用！");
            throw new BusinessException(ResultCodeEnum.FAIL, "账号被禁用");
        }
        log.info("开始请求auth去登录，(地址：/login/unionId)");
        RWrapper<Map<String, String>> results = packData(params);
        log.info("结束请求auth去登录，返回数据={}", results);
        //登录失败，返回错误编码以及错误描述
        if (!"1000".equals(results.getCode())) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", results.getCode());
            jsonObject.put("msg", results.getMsg());
            return jsonObject.toString();
        }
        JSONObject json = null;
        if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
            json = authService.getRongCloudToken(member.getId(), member.getNickname(), member.getFacephotopath(), CommonConfig.FBTX_ROLE_ID);
            log.info("调用融云={}", json);
        }
        JSONObject data = JSONObject.parseObject(JSON.toJSONString(results.getData()));
        data.put("phone", member.getPhone());
        data.put("guid", member.getId());
        data.put("email", member.getEmail());
        if (!StringUtil.isNullOrEmpty(member.getFacephotopath())) {
            if (member.getFacephotopath().contains("http")) {
                data.put("facephotopath", member.getFacephotopath());
            } else {
                data.put("facephotopath", config.getPicture_url() + member.getFacephotopath());
            }
        }
        data.put("gender", member.getGender());
        data.put("name", member.getName());
        data.put("nickName", member.getNickname());
        data.put("idCardNumber", member.getIdcardnumber());
        if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
            data.put("rongCloudToken", json.getString("token"));
            data.put("rongCloudId", json.getString("id"));
        }
        log.info("微信登录成功，返回前台参数为：{}", data);
        return WrapperUtil.ok(data).toString();
    }

    /**
     * 封装请求数据
     *
     * @param param
     * @return
     */
    public RWrapper<Map<String, String>> packData(JSONObject param) {
        log.info("packData入参={},", param);
        String openId = param.getString("openId");
        String unionId = param.getString("unionId");
        String imei = param.getString("imei");
        String macAddr = param.getString("macAddr");
        String ip = param.getString("ip");
        String terminalType = param.getString("terminalType");
        String phoneModel = param.getString("phoneModel");
        String appVersion = param.getString("appVersion");
        String systemVersion = param.getString("systemVersion");
        String channel = param.getString("channel");

        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("unionId", unionId);
        postParameters.add("openId", openId);
        postParameters.add("imei", imei);
        postParameters.add("macAddr", macAddr);
        postParameters.add("ip", ip);
        postParameters.add("terminalType", terminalType);
        postParameters.add("phoneModel", phoneModel);
        postParameters.add("systemVersion", systemVersion);
        postParameters.add("channel", channel);
        postParameters.add("appVersion", appVersion);
        postParameters.add("userRoleId", param.getString("userRoleId"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("authorization", AuthUtil.getBasicAuthHeader(config.getBviClientId(), config.getBviClientSecert()));
        HttpEntity<MultiValueMap<String, Object>> parameter = new HttpEntity<>(postParameters, headers);
        RWrapper<Map<String, String>> results = restTemplate.postForObject("http://auth/login/unionId", parameter, RWrapper.class);
        return results;
    }

}
