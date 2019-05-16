package com.bkgc.bless.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendResult;
import com.bkgc.bean.SearchBean;
import com.bkgc.bean.account.AuthAccount;
import com.bkgc.bean.bless.Company;
import com.bkgc.bean.security.UserTokenParam;
import com.bkgc.bean.security.bean.UserInfoParam;
import com.bkgc.bean.user.AuthCompany;
import com.bkgc.bean.user.AuthMember;
import com.bkgc.bean.user.AuthSpaceMaster;
import com.bkgc.bless.config.Config;
import com.bkgc.bless.config.MqInitializer;
import com.bkgc.bless.config.SignClient;
import com.bkgc.bless.constant.CommonConfig;
import com.bkgc.bless.consumer.AuthFeignService;
import com.bkgc.bless.consumer.GtsFeignService;
import com.bkgc.bless.consumer.MessageFeignService;
import com.bkgc.bless.consumer.PaymentFeignService;
import com.bkgc.bless.mapper.*;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.*;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@Transactional
@Service
public class AuthService {

    @Autowired
    private AuthMemberMapper authMemberMapper;
    @Autowired
    private AuthAccountMapper authAccountMapper;

    @Autowired
    private AuthLoginCredentialMapper authLoginCredentialMapper;

    @Autowired
    private RandomMappingGroupMapper randomMappingGroupMapper;

    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private AuthCompanyMapper authCompanyMapper;

    @Autowired
    private MessageConsumer messageConsumer;

    @Resource
    private Config config;

    @Resource
    private SignClient signClient;

    @Autowired
    private Producer producer;

    @Autowired
    private PaymentFeignService paymentFeignService;

    @Autowired
    private AuthFeignService authFeignService;

    @Autowired
    private GtsFeignService gtsFeignService;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SqlSession sqlSession;
    @Autowired
    private MessageFeignService messageFeignService;

    @Autowired
    private AuthSpaceMasterMapper authSpaceMasterMapper;

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public AuthService() {
    }

    /**
     * 普通登录
     *
     * @param params
     * @return
     */
//    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
//    public JSONObject login(JSONObject params) {
//        String userName = params.getString("userName");
//        String password = params.getString("password");
//        if (StringUtil.isNullOrEmpty(userName)) {
//            throw new BusinessException(ResultCodeEnum.ERR_41047, "用户名不能为空");
//        }
//        if (StringUtil.isNullOrEmpty(password)) {
//            throw new BusinessException(ResultCodeEnum.ERR_41048, "密码不能为空");
//        }
//        JSONObject data = new JSONObject();
//        UserService userService = UserService.getInstance();
//        UserTokenParam tokenParam = new UserTokenParam();
//        tokenParam.setUsername(config.getBviModule().toUpperCase().concat("_").concat(userName));
//        tokenParam.setPassword(password);
//        tokenParam.setClient_id(config.getBviClientId());
//        tokenParam.setClient_secret(config.getBviClientSecert());
//        tokenParam.setGrant_type(config.getBviGrantType());
//        tokenParam.setScope(config.getBviScope());
//        tokenParam.setLogin_type(config.getLoginTypeUp());
//        tokenParam.setModule(config.getBviModule());
//        tokenParam.setRoleid(config.getRoleId());
//        ResultData result = userService.userTokenGet(tokenParam);
//
//        String code = result.getCode();
//        if ("1000".equals(code)) {
//            JSONObject data3 = (JSONObject) result.getData();
//            String guid = data3.getString("guid");
//            AuthMember member = authMemberMapper.selectByguid(guid);
//            data.put("phone", userName);
//            JSONObject data2 = (JSONObject) result.getData();
//            data2.put("guid", member.getId());
//            data2.put("email", member.getEmail());
//            if (!StringUtil.isNullOrEmpty(member.getFacephotopath())) {
//                if (member.getFacephotopath().contains("wx.qlogo.cn")) {
//                    data2.put("facephotopath", member.getFacephotopath());
//                } else {
//                    data2.put("facephotopath", config.getPicture_url() + member.getFacephotopath());
//                }
//            }
//            data2.put("gender", member.getGender());
//            data2.put("name", member.getName());
//            data2.put("nickName", member.getNickname());
//            data2.put("phone", member.getPhone());
//            data2.put("idCardNumber", member.getIdcardnumber());
//            data.put("data", data2);
//            log.info("登录成功，返回前台参数 ：" + data);
//            return ResultUtil.buildSuccessResult(Integer.parseInt(code), data);
//        } else {
//            return ResultUtil.buildFailResult(Integer.parseInt(code), result.getMsg());
//        }
//    }

    /**
     * 通过微信登录
     *
     * @param params
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject loginByWeiXin(JSONObject params) {
        //UserService userService = UserService.getInstance();
        String openId = params.getString("openId");
        String unionId = params.getString("unionid");
        String imei = params.getString("imei");
        String macAddr = params.getString("macAddr");
        String ip = params.getString("ip");
        String terminalType = params.getString("terminalType");
        String phoneModel = params.getString("phoneModel");
        String systemVersion = params.getString("systemVersion");
        String channel = params.getString("channel");
        String appVersion = params.getString("appVersion");
        String loginType = params.getString("loginType");

        String phone = params.getString("phone");

        if (StringUtil.isNullOrEmpty(openId)) {
            throw new BusinessException(ResultCodeEnum.ERR_41049, "openId不能为空");
        }
        //没有手机号，说明是微信登录
        if (StringUtil.isNullOrEmpty(phone)) {
            String username = openId;
            String password = openId;
            UserTokenParam tokenParam = new UserTokenParam();
            tokenParam.setUsername(username);
            tokenParam.setPassword(password);
            tokenParam.setClient_id(config.getBviClientId());
            tokenParam.setClient_secret(config.getBviClientSecert());
            tokenParam.setGrant_type(config.getBviGrantType());
            tokenParam.setScope(config.getBviScope());
            tokenParam.setLogin_type(config.getLoginTypeOPENID());
            tokenParam.setModule(config.getBviModule());
            tokenParam.setRoleid(config.getRoleId());

            //新增字段
            tokenParam.setImei(imei);
            tokenParam.setMacAddr(macAddr);
            tokenParam.setIp(ip);
            tokenParam.setTerminalType(terminalType);
            tokenParam.setPhoneModel(phoneModel);
            tokenParam.setSystemVersion(systemVersion);
            tokenParam.setChannel(channel);
            tokenParam.setAppVersion(appVersion);
            tokenParam.setCheckImei(true);
            tokenParam.setLoginByPhoneCode("0");
            tokenParam.setLoginType(1);

            //ResultData result = userService.userTokenGet(tokenParam);
            MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
            postParameters.add("openId", openId);
            postParameters.add("imei", imei);
            postParameters.add("macAddr", macAddr);
            postParameters.add("ip", ip);
            postParameters.add("terminalType", terminalType);
            postParameters.add("phoneModel", phoneModel);
            postParameters.add("systemVersion", systemVersion);
            postParameters.add("channel", channel);
            postParameters.add("appVersion", appVersion);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/x-www-form-urlencoded");
            headers.add("authorization", AuthUtil.getBasicAuthHeader(config.getBviClientId(), config.getBviClientSecert()));
            HttpEntity<MultiValueMap<String, Object>> parameter = new HttpEntity<>(postParameters, headers);
            RWrapper<JSONObject> result = restTemplate.postForObject("http://" + "auth" + "/login/openId", parameter, RWrapper.class);
            //RWrapper<Map<String,String>> result = restTemplate.postForObject("http://" + "auth" + "/login/openId", parameter, RWrapper.class);

            String code = result.getCode();
            //登录成功
            if ("1000".equals(code)) {
                //JSONObject data = (JSONObject) result.getData();
                String resData = JSON.toJSONString(result.getData());
                JSONObject data = JSON.parseObject(resData);
                //Map<String,String> data = result.getData();
                AuthMember member = authMemberMapper.getAuthMemberByWXOpenId(openId);
                if (member == null) {
                    return ResultUtil.buildFailResult(2020, "该用户不存在");
                }
                if (!StringUtil.isNullOrEmpty(member.getPhone())) {
                    data.put("phone", member.getPhone());
                }
                if (!StringUtil.isNullOrEmpty(member.getId())) {
                    data.put("guid", member.getId());
                }

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

                log.info("第三方登录成功，返回前台参数为：" + data);
                return ResultUtil.buildSuccessResult(Integer.parseInt(code), data);
            } else {
                return ResultUtil.buildFailResult(Integer.parseInt(result.getCode()), result.getMsg());
            }
        } else {
            //第一次微信登录 。即进行第三方注册
            return resisterByOpenId(params);
        }
    }


    private JSONObject resisterByOpenId(JSONObject params) {
        //UserService userService = UserService.getInstance();
        boolean flag = false;
        String flagName = "";
        try {
            log.info("检查该手机号是否已绑定微信");
            System.out.println("微信注册 接口 请求参数：" + params);
            String openId = params.getString("openId");
            String phone = params.getString("phone");
            Random random = new Random();
            int num = random.nextInt(899999) + 100000;

            String password = String.valueOf(num);
            AuthMember b = authMemberMapper.getAuthMemberByPhone(phone);
            if (b != null && !StringUtil.isNullOrEmpty(b.getWeixinopenid())) {
                return ResultUtil.buildFailResult(2003, "该手机号已被绑定！");
            }

            log.info("开始进行第三方注册");
            String nickName = "";
            if (!StringUtil.isNullOrEmpty(params.getString("nickName"))) {
                nickName = params.getString("nickName").replaceAll("[^\\u0000-\\uFFFF]", "*");//source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "*")
            }

            String facePhotoPath = params.getString("facePhotoPath");

            String sex = params.getString("sex");

            String smgCode = params.getString("smgCode");
            if (StringUtil.isNullOrEmpty(smgCode)) {
                throw new BusinessException("手机验证码不能为空");
            }
            /*UserRegParam registParam = new UserRegParam();
            registParam.setOpenid(openId);
            registParam.setPlatform(config.getPlatform());
            registParam.setPhone(phone);
            registParam.setPassword(password);
            registParam.setNickname(nickName);
            registParam.setIconUrl(facePhotoPath);
            registParam.setBackup(sex);
            registParam.setRoleid(config.getRoleId());
            registParam.setCode(smgCode);
            registParam.setUsername("BVI_" + phone);
            registParam.setRoleid(config.getRoleId());
            log.info("第三方注册，请求参数为 ：" + registParam);*/
            //ResultData RegistResult = userService.userReg(registParam);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("openid", openId);
            jsonObject.put("platform", config.getPlatform());
            jsonObject.put("phone", phone);
            jsonObject.put("password", password);
            jsonObject.put("nickName", nickName);
            jsonObject.put("iconUrl", facePhotoPath);
            jsonObject.put("backup", sex);
            jsonObject.put("roleid", config.getRoleId());
            jsonObject.put("code", smgCode);
            jsonObject.put("username", "BVI_" + phone);
            jsonObject.put("roleid", config.getRoleId());
            RWrapper<JSONObject> RegistResult = authFeignService.register(jsonObject);

            String RegistCode = RegistResult.getCode();
            if ("1000".equals(RegistCode)) {//注册成功
                flag = true;
                log.info("第三方在4A平台注册成功 ,4A返回结果为：" + RegistResult);
                JSONObject returnResult = (JSONObject) RegistResult.getData();
                String guid = returnResult.getString("guid");

                flagName = returnResult.getString("username");
                AuthMember authmember = authMemberMapper.getAuthMemberByPhone(phone);
                if (authmember == null) {
                    log.info("创建账户以及member表");
                    //Map<String, Object> insertParams = new LinkedHashMap<String, Object>();
                    JSONObject insertParams = new JSONObject();
                    insertParams.put("userId", guid);
                    insertParams.put("userType", config.getUserType());
                    insertParams.put("clientID", signClient.getClientId());
                    insertParams.put("nonce_str", UUID.randomUUID().toString());
                    String sign = Signature.getSign(insertParams, signClient.getClientSecret());
                    insertParams.put("sign", sign);
                    //JSONObject result = sendPost(config.getPayment_url() + config.getInsertAuthAccount(), insertParams);
                    RWrapper<JSONObject> resultData = paymentFeignService.insertAuthAccount(insertParams);
                    log.info("创建账户以及member表 返回数据={}", resultData);
                    JSONObject result = JSONObject.parseObject(resultData.toString());
                    if (!"1000".equals(result.getString("code"))) {
                        //回滚4A系统  删除用户
                        log.info("账户创建不成功，原因为：" + result.getString("msg") + ",此时回滚4A平台，逻辑删除【" + returnResult.getString("username") + "】该用户");
                        JSONObject delJson = new JSONObject();
                        delJson.put("username", returnResult.getString("username"));
                        RWrapper delWrap = authFeignService.deleteByUserName(delJson);
                        if (!"1000".equals(delWrap.getCode())) {
                            RWrapper delW = authFeignService.deleteByUserName(delJson);
                            log.info("再次回滚4A平台，将【" + returnResult.getString("username") + "】用户进行逻辑删除，删除结果为：" + delW.getCode());
                        }
                        throw new BusinessException("账户创建失败");
                    }

                    log.info("发送mq消息");

                    JSONObject data = new JSONObject();
                    data.put("guid", guid);
                    data.put("nickName", nickName);
                    data.put("phone", phone);
                    Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", data.toJSONString().getBytes());
                    producer.send(message);

                    log.info("插入auth_member一条记录");
                    AuthMember member = new AuthMember();
                    member.setId(guid);
                    member.setGuid(guid);
                    member.setCreatetime(DateTimeUtils.getCurrentDateTime());
                    member.setGender(sex);
                    member.setPhone(phone);
                    member.setFacephotopath(facePhotoPath);
                    member.setNickname(nickName);
                    member.setWeixinopenid(openId);
                    int count = authMemberMapper.insertSelective(member);
                    if (count != 1) {
                        JSONObject delJson = new JSONObject();
                        delJson.put("username", returnResult.getString("username"));
                        RWrapper delWrap = authFeignService.deleteByUserName(delJson);
                        if (!"1000".equals(delWrap.getCode())) {
                            authFeignService.deleteByUserName(delJson);
                        }
                    }
//							//微信注册成功，发送密码短信
//							MessageUtil.sendCodeToPhone(phone, String.valueOf(num));
                } else {
                    log.info("此第三方绑定的手机号已注册，进入更新auth_member表");
                    authmember.setGuid(guid);
                    authmember.setPhone(phone);
                    authmember.setId(guid);
                    authmember.setGender(sex);
                    authmember.setFacephotopath(facePhotoPath);
                    authmember.setNickname(nickName);
                    authmember.setWeixinopenid(openId);
                    authMemberMapper.updateByPrimaryKeySelective(authmember);
                }
                log.info("第三方注册成功，进入4A平台获取token");
                params.remove("phone");
                return loginByWeiXin(params);
//						UserTokenParam tokenParam = new UserTokenParam();
//						tokenParam.setUsername(username);
//						tokenParam.setPassword(username);
//						tokenParam.setClient_id(config.getBviClientId());
//						tokenParam.setClient_secret(config.getBviClientSecert());
//						tokenParam.setGrant_type(config.getBviGrantType());
//						tokenParam.setScope(config.getBviScope());
//						tokenParam.setLogin_type(config.getLoginTypeOPENID());
//						tokenParam.setModule(config.getBviModule());
//						tokenParam.setRoleid(config.getRoleId());
//						ResultData resultData = userService.userTokenGet(tokenParam);
//						if(!"1000".equals(resultData.getCode())){
//							return ResultUtil.buildFailResult(Integer.parseInt(resultData.getCode()),resultData.getMsg());
//						}
//						AuthMember auth = authMemberMapper.getAuthMemberByPhone(phone);
//						JSONObject data = (JSONObject) resultData.getData();
//						data.put("NickName", auth.getNickname());
//						data.put("Name", auth.getName());
//						data.put("Gender", auth.getGender());
//						data.put("FacePhotoPath", auth.getFacephotopath());
//						return ResultUtil.buildSuccessResult(Integer.parseInt(resultData.getCode()), data);
            } else {
                return ResultUtil.buildFailResult(Integer.parseInt(RegistCode), RegistResult.getMsg());
            }
        } catch (NumberFormatException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            log.error("异常错误为：" + e.getMessage());
            return ResultUtil.buildFailResult(4107, e.getMessage());
        } catch (BusinessException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            log.error("异常错误为：" + e.getMessage());
            return ResultUtil.buildFailResult(2020, e.getMessage());
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            if (flag == true) {
                JSONObject delJson = new JSONObject();
                delJson.put("username", flagName);
                RWrapper delWrap = authFeignService.deleteByUserName(delJson);
                if (!"1000".equals(delWrap.getCode())) {
                    authFeignService.deleteByUserName(delJson);
                }
            }
            log.error("异常错误为：" + e.getMessage());
            return ResultUtil.buildFailResult(4105, e.getMessage());
        }
    }


    /**
     * 更新用户基本信息
     *
     * @param params
     * @param request
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject updateUserInfo(JSONObject params, HttpServletRequest request) {
        JSONObject resultData = new JSONObject();
        String sex = request.getParameter("gender");//性别
        String email = params.getString("email");//电子邮箱
        String userId = params.getString("userId");
        String facePhotoPath = params.getString("imgUrl");//头像
        String nickName = params.getString("nickName");//昵称
        String filename = UUID.randomUUID().toString();


        //判断是哪个平台登录  31：福包天下安卓端 32：福包天下苹果端
        String clientID = params.getString("clientID");
        String weiXinNum = null;
        String address = null;
        if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
            //微信号 是收货地址
            weiXinNum = params.getString("weiXinNum");
            address = params.getString("address");
        }

        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.ERR_41050, "请先登录");
        }

        if (!StringUtil.isNullOrEmpty(facePhotoPath)) {
            String path = config.getBlessImgPath().concat("/facePhoto");
            log.info("图片保存地址为：" + path);
            log.info("图片名称为：" + filename);

            int flag = ReadFileUtil.uploadImage(facePhotoPath, filename, path);
            if (flag != 1) {
                throw new BusinessException(ResultCodeEnum.ERR_41051, "上传头像失败");
            }
        } else {
            filename = null;
        }

        int count;
        AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByUserId(userId);
        log.info("空间主={}", authSpaceMaster);
        if (authSpaceMaster != null) {
            log.info("该用户是空间主！");
            authSpaceMaster.setGender(sex);
            authSpaceMaster.setEmail(email);

            if (!StringUtil.isNullOrEmpty(facePhotoPath)) {
                authSpaceMaster.setHeadPortrait("/facePhoto/" + filename + ".jpg");
            }
            authSpaceMaster.setNickname(nickName);
            authSpaceMaster.setWeixinNum(weiXinNum);
            authSpaceMaster.setAddress(address);
            count = authSpaceMasterMapper.updateByPrimaryKeySelective(authSpaceMaster);
            if (count == 1) {
                log.info("开始去message数据库rc_token修改头像");
                try {
                    if (!StringUtil.isNullOrEmpty(nickName) || !StringUtil.isNullOrEmpty(filename)) {
                        JSONObject faceJson = new JSONObject();
                        faceJson.put("userId", userId);
                        if (!StringUtil.isNullOrEmpty(filename)) {
                            faceJson.put("facePath", config.getPicture_url() + "/facePhoto/" + filename + ".jpg");
                        }
                        if (!StringUtil.isNullOrEmpty(nickName)) {
                            faceJson.put("nickName", nickName);
                        }
                        log.info("入参={}", faceJson);
                        RWrapper resData = messageFeignService.updateFace(faceJson);
                        log.info("返回数据={}", resData);
                        if (!"1000".equals(resData.getCode())) {
                            log.info("message数据库rc_token修改头像没有成功！");
                        }
                    }
                } catch (Exception e) {
                    log.info("message数据库rc_token修改头像出现异常！");
                }
                log.info("结束去message数据库rc_token修改头像");



                AuthSpaceMaster spaceMaster = authSpaceMasterMapper.selectByUserId(userId);
                resultData.put("email", spaceMaster.getEmail());
                String facePath = spaceMaster.getHeadPortrait();
                if (!facePath.contains("http")) {
                    facePath = config.getPicture_url() + facePath;
                }
                resultData.put("facephotopath", facePath);
                resultData.put("gender", spaceMaster.getGender());
                resultData.put("name", spaceMaster.getRealName());
                resultData.put("nickName", spaceMaster.getNickname());
                resultData.put("phone", spaceMaster.getPhone());
                resultData.put("weiXinNum", spaceMaster.getWeixinNum());
                resultData.put("address", spaceMaster.getAddress());
                return ResultUtil.buildSuccessResult(1000, resultData);
            } else {
                return ResultUtil.buildSuccessResult(2004, "用户基本信息更新失败");
            }

        } else {
            log.info("该用户不是空间主！");
            AuthMember member = new AuthMember();
            member.setId(userId);
            if (!StringUtil.isNullOrEmpty(weiXinNum)) {
                member.setWeixinNum(weiXinNum);
            }
            if (!StringUtil.isNullOrEmpty(address)) {
                member.setAddress(address);
            }
            if (!StringUtil.isNullOrEmpty(email)) {
                member.setEmail(email);
            }
            if (!StringUtil.isNullOrEmpty(facePhotoPath)) {
                member.setFacephotopath("/facePhoto/" + filename + ".jpg");
            }
            if (!StringUtil.isNullOrEmpty(sex)) {
                member.setGender(sex);
            }
            if (!StringUtil.isNullOrEmpty(nickName)) {
                member.setNickname(nickName);
                JSONObject data = new JSONObject();
                data.put("guid", userId);
                data.put("nickName", nickName);
                log.info("发送mq消息：" + data);
                Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", data.toJSONString().getBytes());
                SendResult send = producer.send(message);
                log.info("mq的消息id：" + send.getMessageId());
            }
            count = authMemberMapper.updateByPrimaryKeySelective(member);

        }
        log.info("开始去message数据库rc_token修改头像");
        try {
            if (!StringUtil.isNullOrEmpty(nickName) || !StringUtil.isNullOrEmpty(filename)) {
                JSONObject faceJson = new JSONObject();
                faceJson.put("userId", userId);
                if (!StringUtil.isNullOrEmpty(filename)) {
                    faceJson.put("facePath", config.getPicture_url() + "/facePhoto/" + filename + ".jpg");
                }
                if (!StringUtil.isNullOrEmpty(nickName)) {
                    faceJson.put("nickName", nickName);
                }
                log.info("入参={}", faceJson);
                RWrapper resData = messageFeignService.updateFace(faceJson);
                log.info("返回数据={}", resData);
                if (!"1000".equals(resData.getCode())) {
                    log.info("message数据库rc_token修改头像没有成功！");
                }
            }
        } catch (Exception e) {
            log.info("message数据库rc_token修改头像出现异常！");
        }
        log.info("结束去message数据库rc_token修改头像");

        //用户基本信息修改成功
        if (count == 1) {
            AuthMember authMember = authMemberMapper.selectByPrimaryKey(userId);
            resultData.put("email", authMember.getEmail());
            String facePath = authMember.getFacephotopath();
            if (!facePath.contains("http")) {
                facePath = config.getPicture_url() + facePath;
            }
            resultData.put("facephotopath", facePath);
            resultData.put("gender", authMember.getGender());
            resultData.put("name", authMember.getName());
            resultData.put("nickName", authMember.getNickname());
            resultData.put("phone", authMember.getPhone());
            resultData.put("weiXinNum", authMember.getWeixinNum());
            resultData.put("address", authMember.getAddress());
            return ResultUtil.buildSuccessResult(1000, resultData);
        } else {
            return ResultUtil.buildSuccessResult(2004, "用户基本信息更新失败");
        }
    }

    /**
     * 通过userId获取用户信息
     *
     * @param params
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject getUserInfo(JSONObject params) {
        JSONObject data = new JSONObject();
        String userId = params.getString("userId");
        String roleId = params.getString("roleId");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.ERR_41050, "请先登录");
        }
        if (roleId != null && CommonConfig.FBTX_SPACE_ROLE_ID.equals(roleId)) {
            AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByUserId(userId);
            log.info("authSpaceMaster={}", authSpaceMaster);
            data.put("email", authSpaceMaster.getEmail());
            if (!StringUtil.isNullOrEmpty(authSpaceMaster.getHeadPortrait())) {
                if (authSpaceMaster.getHeadPortrait().contains("http")) {
                    data.put("facephotopath", authSpaceMaster.getHeadPortrait());
                } else {
                    data.put("facephotopath", config.getPicture_url() + authSpaceMaster.getHeadPortrait());
                }
            }
            data.put("gender", authSpaceMaster.getGender());
            data.put("name", authSpaceMaster.getRealName());
            String nickName = authSpaceMaster.getNickname();
            String phone = authSpaceMaster.getPhone();
            /*String front = phone.substring(0, 3);
            String alert = phone.substring(phone.length() - 2);
            if (nickName.equals(phone)) {
                nickName = front + "******" + alert;
            }*/
            data.put("nickName", nickName);
            data.put("phone", phone);
            data.put("idCardNumber", authSpaceMaster.getIdcardNumber());
            data.put("title", authSpaceMaster.getTitle());
            data.put("weiXinNum", authSpaceMaster.getWeixinNum());
            data.put("address", authSpaceMaster.getAddress());
        } else {
            AuthMember member = authMemberMapper.selectByPrimaryKey(userId);
            if (member == null) {
                //return ResultUtil.buildFailResult(2002, "该用户不存在");
                throw new BusinessException(ResultCodeEnum.USER_NOTEXIST, "该用户不存在");
            }
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
            String nickName = member.getNickname();
            String phone = member.getPhone();
            String front = phone.substring(0, 3);
            String alert = phone.substring(phone.length() - 2);
            if (roleId != null && CommonConfig.FBTX_ROLE_ID.equals(roleId)) {
                if (nickName.equals(phone)) {
                    nickName = front + "******" + alert;
                }
                //phone = front + "******" + alert;
            }
            data.put("nickName", nickName);
            data.put("phone", phone);
            data.put("idCardNumber", member.getIdcardnumber());
            data.put("title", member.getTitle());
            data.put("weiXinNum", member.getWeixinNum());
            data.put("address", member.getAddress());
        }
        return ResultUtil.buildSuccessResult(1000, data);
    }


    /**
     * 更新用户真实信息
     *
     * @param params
     * @return
     */

    public JSONObject updateRealInfo(JSONObject params) {
        JSONObject data = new JSONObject();
        String userId = params.getString("userId");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.ERR_41058, "用户编号不能为空");
        }
        String name = params.getString("name");
        if (StringUtil.isNullOrEmpty(name)) {
            throw new BusinessException(ResultCodeEnum.ERR_41059, "用户姓名不能为空");
        }
        String idCardNumber = params.getString("idCardNumber");
        if (StringUtil.isNullOrEmpty(idCardNumber)) {
            throw new BusinessException(ResultCodeEnum.ERR_41060, "身份证号不能为空");
        }

        JSONObject checkInfo = new JSONObject();

        checkInfo.put("userId", userId);
        checkInfo.put("userType", "1");
        checkInfo.put("idCardNum", idCardNumber);
        checkInfo.put("realName", name);

        RWrapper rWrapper = authFeignService.checkIdCardCheck(checkInfo);
        log.info("身份校验服务返回={}", rWrapper.toString());

        if (!"1000".equals(rWrapper.getCode())) {
            if ("2061".equals(rWrapper.getCode())) {
                throw new BusinessException(ResultCodeEnum.ERR_2061, "身份信息不匹配");
            } else if ("2062".equals(rWrapper.getCode())) {
                throw new BusinessException(ResultCodeEnum.ERR_2062, "无此身份证号");
            } else if ("2063".equals(rWrapper.getCode())) {
                throw new BusinessException(ResultCodeEnum.ERR_2063, "今日实名次数已达5次上限");
            } else if ("2064".equals(rWrapper.getCode())) {
                throw new BusinessException(ResultCodeEnum.ERR_2064, "该身份证号码已被实名");
            } else {
                throw new BusinessException("身份校验服务系统繁忙，请稍后再试");
            }
        }

        JSONObject resJson = JSONObject.parseObject(JSONObject.toJSONString(rWrapper.getData()));
        String sex = resJson.getString("sex");
        log.info("性别={}", sex);
        JSONObject json = new JSONObject();
        json.put("userName", name);
        json.put("guid", userId);
        log.info("发送mq消息：" + json);
        Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", json.toJSONString().getBytes());
        SendResult send = producer.send(message);
        log.info("mq的消息id：" + send.getMessageId());

        AuthMember authMember = new AuthMember();
        authMember.setName(name);
        authMember.setIdcardnumber(idCardNumber);
        authMember.setId(userId);
        authMember.setGender(sex);
        authMemberMapper.updateByPrimaryKeySelective(authMember);
        data.put("result", "更新用户真实信息成功");
        return ResultUtil.buildSuccessResult(1000, data);
    }

    /**
     * 手机号注册用户
     *
     * @param params
     * @return
     */
    //@Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject registerWithPhone(JSONObject params) {
        log.info("用户注册入参{}", params.toString());
        String phone = params.getString("phone");
        String password = params.getString("password");
        //选填，如为空，则默认为手机号
        String nickName = params.getString("nickName");
        String email = params.getString("email");
        //选填，如为空，则，默认设置为手机号
        String username = params.getString("userName");
        String smsCode = params.getString("smsCode");
        String clientID = params.getString("clientID");

        if (StringUtil.isNullOrEmpty(phone)) {
            throw new BusinessException(ResultCodeEnum.ERR_41052, "手机号不能为空");
        }
        if (StringUtil.isNullOrEmpty(password)) {
            throw new BusinessException(ResultCodeEnum.ERR_41048, "密码不能为空");
        }
        if (StringUtil.isNullOrEmpty(smsCode)) {
            throw new BusinessException(ResultCodeEnum.ERR_41053, "手机验证码不能为空");
        }

        JSONObject paramJson = new JSONObject();
        paramJson.put("phone", phone);
        paramJson.put("password", password);
        if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
            //8：福包天下用户
            paramJson.put("roleid", CommonConfig.FBTX_ROLE_ID);
            paramJson.put("platform", CommonConfig.USERNAME_PREFIX);
        } else {
            paramJson.put("roleid", config.getRoleId());
            paramJson.put("platform", config.getPlatform());
        }


        if (!StringUtil.isNullOrEmpty(username)) {
            if (!isContainChinese(username)) {
                paramJson.put("username", username);
            } else {
                throw new BusinessException(ResultCodeEnum.ERR_41054, "用户名不能包含汉字");
            }
        }
        if (!StringUtil.isNullOrEmpty(email)) {
            paramJson.put("email", email);
        }

        if (!StringUtil.isNullOrEmpty(nickName)) {
            paramJson.put("nickName", nickName);
        } else {
            if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                String front = phone.substring(0, 3);
                String alert = phone.substring(phone.length() - 2);
                nickName = front + "******" + alert;
                paramJson.put("nickName", nickName);
            } else {
                paramJson.put("nickName", phone);
            }
        }

        paramJson.put("iconUrl", "");
        paramJson.put("backup", "");
        paramJson.put("code", smsCode);
        paramJson.put("unionId", "");

        //payment中需要
        paramJson.put("userType", config.getUserType());

        /*RWrapper rWrapper = messageConsumer.verify(phone, smsCode);
        log.info("验证手机验证码结果为:" + rWrapper.toString());
        if (!rWrapper.getCode().equals("1000")) {
            log.error("验证手机验证码失败={}", rWrapper.toString());
            throw new BusinessException(ResultCodeEnum.MSG_CHECK_FAIL, "验证码错误");
        }*/


        //RWrapper<JSONObject> result = authFeignService.register(paramJson);
        RWrapper<JSONObject> result = gtsFeignService.register(paramJson);
        log.info("注册返回数据={}", result);
        if ("1000".equals(result.getCode())) {
            return JSONObject.parseObject(result.toString());
        } else {
            throw new BusinessException(ResultCodeEnum.FAIL, result.getMsg());
        }
        //=================================================

/*        JSONObject delJson = new JSONObject();
        //4A上注册成功
        if ("1000".equals(result.getCode())) {
            log.info("手机号" + phone + "在4A上创建成功，开始创建账户");
            JSONObject userReturn = result.getData();
            String guid = (String) userReturn.get("guid");
            //建立账户
            //Map<String, Object> insertParams = new LinkedHashMap<String, Object>();
            JSONObject insertParams = new JSONObject();
            insertParams.put("userId", guid);
            insertParams.put("userType", config.getUserType());
            insertParams.put("clientID", signClient.getClientId());
            insertParams.put("nonce_str", UUID.randomUUID().toString());
            String sign = Signature.getSign(insertParams, signClient.getClientSecret());
            insertParams.put("sign", sign);
            log.info("创建账户接口，请求参数为：" + insertParams);
            //JSONObject insertResult = sendPost(config.getPayment_url() + config.getInsertAuthAccount(), insertParams);
            RWrapper<JSONObject> insertResultData = paymentFeignService.insertAuthAccount(insertParams);
            log.info("创建账户接口返回数据={}", insertResultData);
            JSONObject insertResult = JSONObject.parseObject(insertResultData.toString());
            if (!"1000".equals(insertResult.getString("code"))) {
                //4A上回滚用户
                log.info("账户创建不成功，对4A平台上的【" + userReturn.getString("username") + "】用户进行逻辑删除！");
                //删除使用feign的方式，将bkgc-security项目废弃掉
                delJson.put("username", userReturn.getString("username"));
                RWrapper deleteByUserName = authFeignService.deleteByUserName(delJson);
                if (!"1000".equals(deleteByUserName.getCode())) {
                    log.info("4A删除不成功，再次进行逻辑删除！");
                    //使用feign的方式，将bkgc-security项目废弃掉
                    authFeignService.deleteByUserName(delJson);
                }
                //throw new Exception("账户创建失败");
                throw new BusinessException(ResultCodeEnum.ERR_41055, "账户创建失败");
            }
            JSONObject data = new JSONObject();
            log.info("将新记录插入Auth_member表！");
            AuthMember auth = authMemberMapper.getAuthMemberByPhone(phone);
            if (auth != null) {
                auth.setId(guid);
                auth.setGuid(guid);
                auth.setEmail(email);
                if (!StringUtil.isNullOrEmpty(nickName)) {

                    auth.setNickname(nickName);
                } else {
                    auth.setNickname(phone);
                }
                authMemberMapper.updateByPrimaryKey(auth);
                data.put("userId", auth.getId());
                return ResultUtil.buildSuccessResult(1000, data);
            } else {
                JSONObject mqData = new JSONObject();
                AuthMember member = new AuthMember();
                member.setId(guid);
                member.setGuid(guid);
                member.setCreatetime(DateTimeUtils.getCurrentDateTime());
                member.setEmail(email);
                member.setPhone(phone);
                if (!StringUtil.isNullOrEmpty(nickName)) {
                    mqData.put("nickName", nickName);
                    member.setNickname(nickName);
                } else {
                    member.setNickname(phone);
                    mqData.put("nickName", phone);
                }

                mqData.put("guid", guid);
                mqData.put("phone", phone);
                log.info("发送mq消息：" + mqData);
                Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", mqData.toJSONString().getBytes());
                SendResult send = producer.send(message);
                log.info("mq的消息id：" + send.getMessageId());
                int count = authMemberMapper.insertSelective(member);
                if (count == 1) {
                    data.put("userId", member.getId());
                    return ResultUtil.buildSuccessResult(1000, data);
                } else {
                    //使用feign的方式，直接掉auth
                    delJson.put("username", userReturn.getString("username"));
                    RWrapper deleteByUserName = authFeignService.deleteByUserName(delJson);
                    if (!"1000".equals(deleteByUserName.getCode())) {
                        log.info("4A删除不成功，再次进行逻辑删除！");
                        //使用feign的方式直接掉auth
                        authFeignService.deleteByUserName(delJson);
                    }
                    throw new BusinessException(ResultCodeEnum.ERR_41055, "账户创建失败");
                }
            }
        } else {
            return ResultUtil.buildFailResult(Integer.parseInt(result.getCode()), result.getMsg().toString());
        }*/
    }

    /**
     * 通过手机号检查用户是否存在，如果不存在，创建用户(不需要token)
     *
     * @param params
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject checkUser(JSONObject params) {
        String phone = params.getString("phone");
        if (StringUtil.isNullOrEmpty(phone)) {
            return ResultUtil.buildFailResult(2020, "请输入手机号");
        }
        String code = params.getString("smgCode");
        if (StringUtil.isNullOrEmpty(code)) {
            return ResultUtil.buildFailResult(2020, "请输入验证码");
        }
        //UserService userService = UserService.getInstance();
        JSONObject data = new JSONObject();
        UserInfoParam userInfoParam = new UserInfoParam();
        userInfoParam.setPhone(phone);
        userInfoParam.setRoleid(config.getRoleId());
        //ResultData resultData = userService.getUserInfoByPhone(userInfoParam);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phone", phone);
        jsonObject.put("roleid", config.getRoleId());
        RWrapper<JSONObject> resultData = authFeignService.getUserInfoByPhone(jsonObject);
        //数据库中存在此手机号注册的用户
        if ("1000".equals(resultData.getCode())) {
            RWrapper r = messageConsumer.verify(phone, code);
            if (!"1000".equals(r.getCode())) {
                return ResultUtil.buildFailResult(2040, "请输入正确的验证码");
            }
            JSONObject jsonData = (JSONObject) resultData.getData();
            String guid = jsonData.getString("guid");
            data.put("UserId", guid);
            return ResultUtil.buildSuccessResult(1000, data);
        } else {
            //数据库中不存在，需要通过手机号注册
            JSONObject registParams = new JSONObject();
            registParams.put("phone", phone);
            registParams.put("password", phone);
            registParams.put("smsCode", code);
            JSONObject jsonResult = registerWithPhone(registParams);
            String resultCode = jsonResult.getString("code");
            if ("1000".equals(resultCode)) {
                JSONObject object = (JSONObject) jsonResult.get("data");
                String userId = (String) object.get("userId");
                data.put("UserId", userId);
                return ResultUtil.buildSuccessResult(1000, data);
            } else {
                return ResultUtil.buildFailResult(Integer.parseInt(jsonResult.get("code").toString()), jsonResult.get("msg").toString());
            }
        }
    }


    /**
     * 判断是否包含汉字
     *
     * @param str
     * @return
     */
    public static boolean isContainChinese(String str) {

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 获取个人账户  总资产，福金余额  ，可提现余额（未用）
     *
     * @param params
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject getAccountInfo(JSONObject params) {
        String userId = params.getString("userId");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.ERR_41050, "请先登录");
        }
        AuthAccount account = authAccountMapper.selectByUserId(userId);
        BigDecimal accountbalance = account.getAccountbalance();//可提现余额
        BigDecimal blessamount = account.getBlessamount();//总账户资产
        BigDecimal subamount = blessamount.subtract(accountbalance);//福金余额

        JSONObject data = new JSONObject();
        data.put("accountbalance", accountbalance);
        data.put("blessamount", blessamount);
        data.put("subamount", subamount);
        return ResultUtil.buildSuccessResult(1000, data);
    }

    /**
     * 用户修改登录密码
     *
     * @param params
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject updatePassword(JSONObject params) {
        String password = params.getString("password");
        String guid = params.getString("userId");
        String token = params.getString("access_token");
        String phone = params.getString("phone");
        String code = params.getString("smgCode");

        if (StringUtil.isNullOrEmpty(phone)) {
            throw new BusinessException(ResultCodeEnum.ERR_41056, "请输入绑定的手机号");
        }
        if (StringUtil.isNullOrEmpty(password)) {
            throw new BusinessException(ResultCodeEnum.ERR_41048, "密码不能为空");
        }
        if (StringUtil.isNullOrEmpty(code)) {
            throw new BusinessException(ResultCodeEnum.ERR_41053, "请输入获取的验证码");
        }
        if (StringUtil.isNullOrEmpty(token)) {
            throw new BusinessException(ResultCodeEnum.ERR_41057, "token不能为空");
        }
        //UserService userService = UserService.getInstance();
        /*UserInfoParam userInfoParam = new UserInfoParam();
        userInfoParam.setGuid(guid);
        userInfoParam.setAccess_token(token);*/
        //ResultData userInfoGet = userService.userInfoGet(userInfoParam);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("guid", guid);
        jsonObject.put("access_token", token);
        RWrapper<JSONObject> userInfoGet = authFeignService.userGet(jsonObject);
        if (!"1000".equals(userInfoGet.getCode())) {
            return ResultUtil.buildFailResult(Integer.parseInt(userInfoGet.getCode()), userInfoGet.getMsg());
        }
        JSONObject data = (JSONObject) userInfoGet.getData();
        String bingPhone = data.getString("phone");
        if (!phone.equals(bingPhone)) {
            return ResultUtil.buildFailResult(4108, "请输入之前绑定的手机号");
        }
        RWrapper r = messageConsumer.verify(phone, code);
        if ("1000".equals(r.getCode())) {//手机验证成功
            /*UserUpdateParam updateParams = new UserUpdateParam();
            updateParams.setAccess_token(token);
            updateParams.setGuid(guid);
            updateParams.setPassword(password);*/
            //ResultData updateResult = userService.userUpdate(updateParams);

            JSONObject updateJson = new JSONObject();
            updateJson.put("token", token);
            updateJson.put("guid", guid);
            updateJson.put("password", password);
            RWrapper<JSONObject> updateResult = authFeignService.updateUserInfo(updateJson);
            if ("1000".equals(updateResult.getCode())) {
                return ResultUtil.buildSuccessResult(1000, updateResult.getData());
            } else {
                return ResultUtil.buildFailResult(Integer.parseInt(updateResult.getCode()), updateResult.getMsg());
            }
        } else {
            return ResultUtil.buildFailResult(Integer.parseInt(r.getCode()), r.getMsg());
        }
    }


    /**
     * 用户修改绑定手机号
     *
     * @param params
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public JSONObject updatePhone(JSONObject params) {
        log.info("进去用户修改绑定手机号方法params={}", params);
        String guid = params.getString("userId");
        String token = params.getString("access_token");
        String phone = params.getString("phone");
        String code = params.getString("smgCode");

        if (StringUtil.isNullOrEmpty(phone)) {
            throw new BusinessException(ResultCodeEnum.ERR_41052, "请输入绑定的手机号");
        }
        if (StringUtil.isNullOrEmpty(code)) {
            throw new BusinessException(ResultCodeEnum.ERR_41053, "请输入获取的验证码");
        }

        RWrapper r = messageConsumer.verify(phone, code);
        //手机验证成功
        if ("1000".equals(r.getCode())) {
            //判断是哪个平台登录  31：福包天下安卓端 32：福包天下苹果端
            String clientID = params.getString("clientID");
            if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                log.info("福包天下用户修改手机号！");
                AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByUserId(guid);
                log.info("空间主={}", authSpaceMaster);
                if (authSpaceMaster != null) {
                    log.info("是空间主");
                    JSONObject updateJson = new JSONObject();
                    updateJson.put("guid", guid);
                    updateJson.put("phone", phone);
                    updateJson.put("roleId", CommonConfig.FBTX_SPACE_ROLE_ID);
                    updateJson.put("username", CommonConfig.USERNAME_PREFIX + "_" + phone);
                    log.info("开始去auth修改");
                    RWrapper<JSONObject> updateResult = authFeignService.updateUserInfo(updateJson);
                    log.info("结束去auth修改，返回数据={}", updateResult);

                    if ("1000".equals(updateResult.getCode())) {
                        log.info("修改成功！");
                        JSONObject json = new JSONObject();
                        json.put("phone", phone);
                        json.put("guid", guid);
                        log.info("发送mq消息：" + json);
                        Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", json.toJSONString().getBytes());
                        SendResult send = producer.send(message);
                        log.info("mq的消息id：" + send.getMessageId());
                        AuthSpaceMaster spaceMaster = authSpaceMasterMapper.selectByUserId(guid);
                        spaceMaster.setPhone(phone);
                        int count = authSpaceMasterMapper.updateByPrimaryKeySelective(spaceMaster);
                        log.info("count={}", count);
                        if (count == 1) {
                            return ResultUtil.buildSuccessResult(1000, "用户更新成功！");
                        } else {
                            return ResultUtil.buildSuccessResult(2004, "用户更新失败");
                        }
                    } else {
                        if (Integer.parseInt(updateResult.getCode()) == 2000) {
                            return ResultUtil.buildFailResult(Integer.parseInt(updateResult.getCode()), "用户信息更新失败");
                        } else {
                            return ResultUtil.buildFailResult(Integer.parseInt(updateResult.getCode()), updateResult.getMsg());
                        }
                    }
                } else {
                    log.info("不是空间主");
                    JSONObject updateJson = new JSONObject();
                    updateJson.put("guid", guid);
                    updateJson.put("phone", phone);
                    updateJson.put("roleId", CommonConfig.FBTX_ROLE_ID);
                    updateJson.put("username", CommonConfig.USERNAME_PREFIX + "_" + phone);
                    log.info("开始去auth修改");
                    RWrapper<JSONObject> updateResult = authFeignService.updateUserInfo(updateJson);
                    log.info("结束去auth修改，返回数据={}", updateResult);
                    if ("1000".equals(updateResult.getCode())) {
                        log.info("修改成功！");
                        JSONObject json = new JSONObject();
                        json.put("phone", phone);
                        json.put("guid", guid);
                        log.info("发送mq消息：" + json);
                        Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", json.toJSONString().getBytes());
                        SendResult send = producer.send(message);
                        log.info("mq的消息id：" + send.getMessageId());
                        AuthMember member = new AuthMember();
                        member.setId(guid);
                        member.setPhone(phone);
                        int count = authMemberMapper.updateByPrimaryKeySelective(member);
                        log.info("count={}", count);
                        if (count == 1) {
                            //return ResultUtil.buildSuccessResult(1000, updateResult.getData());
                            return ResultUtil.buildSuccessResult(1000, "用户更新成功！");
                        } else {
                            return ResultUtil.buildSuccessResult(2004, "用户更新失败");
                        }
                    } else {
                        if (Integer.parseInt(updateResult.getCode()) == 2000) {
                            return ResultUtil.buildFailResult(Integer.parseInt(updateResult.getCode()), "用户信息更新失败");
                        } else {
                            return ResultUtil.buildFailResult(Integer.parseInt(updateResult.getCode()), updateResult.getMsg());
                        }
                    }
                }
            } else {
                JSONObject updateJson = new JSONObject();
                updateJson.put("token", token);
                updateJson.put("guid", guid);
                updateJson.put("phone", phone);
                updateJson.put("module", config.getBviModule());
                updateJson.put("roleId", config.getRoleId());
                updateJson.put("username", config.getBviModule().toUpperCase() + "_" + phone);
                log.info("开始去auth修改");
                RWrapper<JSONObject> updateResult = authFeignService.updateUserInfo(updateJson);
                log.info("结束去auth修改，返回数据={}", updateResult);
                if ("1000".equals(updateResult.getCode())) {
                    log.info("修改成功！");
                    JSONObject json = new JSONObject();
                    json.put("phone", phone);
                    json.put("guid", guid);
                    log.info("发送mq消息：" + json);
                    Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", json.toJSONString().getBytes());
                    SendResult send = producer.send(message);
                    log.info("mq的消息id：" + send.getMessageId());
                    AuthMember member = new AuthMember();
                    member.setId(guid);
                    member.setPhone(phone);
                    int count = authMemberMapper.updateByPrimaryKeySelective(member);
                    log.info("count={}", count);
                    if (count == 1) {
                        //return ResultUtil.buildSuccessResult(1000, updateResult.getData());
                        return ResultUtil.buildSuccessResult(1000, "用户更新成功！");
                    } else {
                        return ResultUtil.buildSuccessResult(2004, "用户更新失败");
                    }
                } else {
                    if (Integer.parseInt(updateResult.getCode()) == 2000) {
                        return ResultUtil.buildFailResult(Integer.parseInt(updateResult.getCode()), "用户信息更新失败");
                    } else {
                        return ResultUtil.buildFailResult(Integer.parseInt(updateResult.getCode()), updateResult.getMsg());
                    }
                }
            }
        } else {
            return ResultUtil.buildFailResult(Integer.parseInt(r.getCode()), r.getMsg());
        }
    }


    /**
     * 获取头像图片地址
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
    public void getImage(HttpServletRequest request, HttpServletResponse response) throws
            IOException, ServletException {
        String filePath = decodeUrl(request.getParameter("p"));
        filePath = (config.getBlessImgPath().concat(filePath)).replace("\\", File.separator).replace("/", File.separator).replace("//", File.separator).replace("/", File.separator);
        ReadFileUtil.read(response, filePath);
    }

    public String decodeUrl(String paramter) {
        try {
            return URLDecoder.decode(paramter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        throw new RuntimeException();
    }

/*    private static JSONObject sendPost(String url, Map<String, Object> params) {
        JSONObject data = new JSONObject();
        ResultData retData = new ResultData();
        String param = "";

        try {
            if (!params.isEmpty() && !StringUtil.isNullOrEmpty(url)) {
                Iterator<String> iterators = params.keySet().iterator();
                while (iterators.hasNext()) {
                    String key = iterators.next();
                    String value = (String) params.get(key);
                    param = param.concat("&").concat(key).concat("=").concat(value);
                }
                if (param.length() != 0) {
                    param = param.substring(1);
                }
            }

            String result = HttpRequest.sendPost(url, param);
            data = JSONObject.parseObject(result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return data;
    }*/

    /**
     * 发送消息
     *
     * @param
     * @return
     */
/*    public JSONObject sendMessage(JSONObject requestParam) {
        String phone = requestParam.getString("phone");
        if (StringUtil.isNullOrEmpty(phone)) {
            throw new BusinessException(ResultCodeEnum.ERR_41052, "请输入绑定的手机号");
        }
        String url = config.getOauth_url() + "/" + config.getMessage_url();
        Map<String, Object> sendParams = new LinkedHashMap<String, Object>();
        sendParams.put("phone", phone);
        sendParams.put("client_id", config.getBviClientId());
        sendParams.put("client_secret", config.getBviClientSecert());
        JSONObject data = sendPost(url, sendParams);
        return data;
    }*/
    public void test() {
        List<AuthMember> all = authMemberMapper.getAll();
/*        if (all.size() != 0) {
            for (int i = 0; i < all.size(); i++) {
                AuthLoginCredential authLogin = authLoginCredentialMapper.getLoginInfoByUserId(all.get(i).getId());
                if (authLogin != null) {
                    Map<String, Object> registParams = new LinkedHashMap<String, Object>();
                    registParams.put("platform", config.getPlatform());
                    registParams.put("phone", all.get(i).getPhone());
                    registParams.put("password", authLogin.getPassword());
                    registParams.put("roleid", config.getRoleId());
                    if (!StringUtil.isNullOrEmpty(authLogin.getUsername())) {
                        registParams.put("username", "BVI_" + authLogin.getUsername());
                    }
                    if (!StringUtil.isNullOrEmpty(all.get(i).getEmail())) {
                        registParams.put("email", all.get(i).getEmail());
                    }
                    if (!StringUtil.isNullOrEmpty(all.get(i).getWeixinopenid())) {
                        registParams.put("openid", all.get(i).getWeixinopenid());
                    }
                    if (!StringUtil.isNullOrEmpty(all.get(i).getNickname())) {
                        registParams.put("nickname", all.get(i).getNickname());
                    }
                    if (!StringUtil.isNullOrEmpty(all.get(i).getFacephotopath())) {
                        registParams.put("iconurl", all.get(i).getFacephotopath());
                    }
                    if (!StringUtil.isNullOrEmpty(all.get(i).getGender())) {
                        registParams.put("backup", all.get(i).getGender());
                    }

                    JSONObject data = sendPost("http://123.56.82.85:20010/oauth/user/registerWithoutCode", registParams);
                    if ("1000".equals(data.getString("code"))) {
                        JSONObject resultData = data.getJSONObject("data");
                        String guid = resultData.getString("guid");
                        all.get(i).setGuid(guid);
                        //all.get(i).setId(guid);
                        int updateByPrimaryKeySelective = authMemberMapper.updateByPrimaryKeySelective(all.get(i));
                        System.out.println(updateByPrimaryKeySelective);
                    } else {
                        System.err.println(all.get(i).getPhone());
                    }
                }
            }
        }*/

/*			List<Company> all = companyMapper.getAll(null, null, null);
            if(all.size() != 0){
				for (Company company : all) {
					AuthLoginCredential a = authLoginCredentialMapper.selectByPrimaryKey(company.getLoginId());
					if(a != null){
						Map<String, Object> registParams = new LinkedHashMap<String, Object>();
						registParams.put("platform", "bvms");
						registParams.put("phone",company.getPhone() );
						registParams.put("password", a.getPassword());
						registParams.put("roleid", "28");
						if(!StringUtil.isNullOrEmpty(a.getUsername())){
							registParams.put("username", "bvms-"+a.getUsername());
						}
						if(!StringUtil.isNullOrEmpty(company.getEmail())){
							registParams.put("email", company.getEmail());
						}

						if(!StringUtil.isNullOrEmpty(company.getName())){
							registParams.put("nickname", company.getName());
						}

						if(!StringUtil.isNullOrEmpty(company)){
							registParams.put("iconurl", a.getFacephotopath());
						}
						if(!StringUtil.isNullOrEmpty(a.getGender())){
							registParams.put("backup", a.getGender());
						}

						JSONObject data = sendPost("https://s20010.8fubao.com/oauth/user/registerWithoutCode", registParams);
						if("1000".equals(data.getString("code"))){
							JSONObject resultData = data.getJSONObject("data");
							String guid = resultData.getString("guid");
							company.setGuid(guid);
							companyMapper.update(company);
						}
					}

				}
			}*/


/*			List<SiteAdministrator> alls = siteAdministratorMapper.getAll(null, null, null);
            if(alls.size() != 0){
				for (SiteAdministrator s : alls) {
					AuthLoginCredential a = authLoginCredentialMapper.selectByPrimaryKey(s.getLoginId());
					if(a != null){
						Map<String, Object> registParams = new LinkedHashMap<String, Object>();
						registParams.put("platform", "bvms");
						registParams.put("phone","13800000000");
						registParams.put("password", a.getPassword());
						registParams.put("roleid", "2");
						if(!StringUtil.isNullOrEmpty(a.getUsername())){
							registParams.put("username", "bvms-"+a.getUsername());
						}
						if(!StringUtil.isNullOrEmpty(s.getEmail())){
							registParams.put("email",s.getEmail());
						}

						if(!StringUtil.isNullOrEmpty(s.getName())){
							registParams.put("nickname", s.getName());
						}
						if(!StringUtil.isNullOrEmpty(s.getFacephotopath())){
							registParams.put("iconurl", a.getFacephotopath());
						}
						if(!StringUtil.isNullOrEmpty(a.getGender())){
							registParams.put("backup", a.getGender());
						}
						JSONObject data = sendPost("https://s20010.8fubao.com/oauth/user/registerWithoutCode", registParams);
						if("1000".equals(data.getString("code"))){
							JSONObject resultData = data.getJSONObject("data");
							String guid = resultData.getString("guid");
							s.setGuid(guid);
							siteAdministratorMapper.update(s);
						}
					}
				}
			}*/

    }


/*public static void main(String[] args) {
     String string = "0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";
	 String[] split = string.split(",");
	 String str = "";
	 for(int i=0;i<7;i++){

		 Random random = new Random();
		 int nextInt = random.nextInt(split.length-1);
		 str+=split[nextInt];
	 }
	 System.out.println(str);
}*/

//    public JSONObject createRandomNumber() {
//        JSONObject data = new JSONObject();
//        String string = "0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";
//        String[] split = string.split(",");
//        String[] arr = new String[100];
//        for (int j = 0; j < 100; j++) {
//
//            String str = "";
//            for (int i = 0; i < 7; i++) {
//
//                Random random = new Random();
//                int nextInt = random.nextInt(split.length - 1);
//                str += split[nextInt];
//            }
//            RandomMappingGroup r = new RandomMappingGroup();
//            r.setRandomNumber(str);
//            int result = randomMappingGroupMapper.insert(r);
//
//            if (result == 1) {
//                arr[j] = str;
//            }
//        }
//        List<RandomMappingGroup> selectNoGroupId = randomMappingGroupMapper.selectNoGroupId();
//        data.put("size", selectNoGroupId.size());
//        data.put("result", arr);
//        return data;
//    }

//    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
//    public JSONObject registerWithoutPhone(JSONObject params) {
//        log.info("进入无需验证码注册用户接口，参数为：" + params);
//        String phone = params.getString("phone");
//        String password = params.getString("password");
//        //选填，如为空，则默认为手机号
//        String nickName = params.getString("nickName");
//        String email = params.getString("email");
//        //选填，如为空，则，默认设置为手机号
//        String username = params.getString("userName");
//
//        if (StringUtil.isNullOrEmpty(phone)) {
//            throw new BusinessException(ResultCodeEnum.ERR_41052, "手机号不能为空");
//        }
//        if (StringUtil.isNullOrEmpty(password)) {
//            throw new BusinessException(ResultCodeEnum.ERR_41048, "密码不能为空");
//        }
//        UserService userService = UserService.getInstance();
//        UserRegParam registParam = new UserRegParam();
//        registParam.setPlatform("bvms");
//        registParam.setPhone(phone);
//        registParam.setPassword(password);
//        if (!StringUtil.isNullOrEmpty(nickName)) {
//            registParam.setNickname(nickName);
//        } else {
//            registParam.setNickname(phone);
//        }
//        registParam.setRoleid("10");
//        if (!StringUtil.isNullOrEmpty(email)) {
//            registParam.setEmail(email);
//        }
//        if (!StringUtil.isNullOrEmpty(username)) {
//            if (!isContainChinese(username)) {
//                registParam.setUsername(username);
//            } else {
//                throw new BusinessException(ResultCodeEnum.ERR_41054, "用户名不能包含汉字");
//            }
//        } else {
//            registParam.setUsername(phone);
//        }
//        ResultData result = userService.userRegWithoutValiCode(registParam);
//        if ("1000".equals(result.getCode())) {//4A上注册成功
//            JSONObject userReturn = (JSONObject) result.getData();
//            String id = UUID.randomUUID().toString();
//            String guid = (String) userReturn.get("guid");
//            //建立账户
//            //Map<String, Object> insertParams = new LinkedHashMap<String, Object>();
//            JSONObject insertParams = new JSONObject();
//            insertParams.put("userId", id);
//            insertParams.put("userType", config.getUserType());
//            insertParams.put("clientID", signClient.getClientId());
//            insertParams.put("nonce_str", UUID.randomUUID().toString());
//            String sign = Signature.getSign(insertParams, signClient.getClientSecret());
//            insertParams.put("sign", sign);
//            //JSONObject insertResult = sendPost(config.getPayment_url() + config.getInsertAuthAccount(), insertParams);
//            RWrapper<JSONObject> insertResult = paymentFeignService.insertAuthAccount(insertParams);
//            log.info("建立账户返回数据={}", insertResult);
//            if (!"1000".equals(insertResult.getCode())) {
//                //throw new Exception("账户创建失败");
//                throw new BusinessException(ResultCodeEnum.ERR_41055, "账户创建失败");
//            }
//
//            AuthMember member = new AuthMember();
//            member.setId(id);
//            member.setGuid(guid);
//            member.setCreatetime(DateTimeUtils.getCurrentDateTime());
//            member.setEmail(email);
//            member.setPhone(phone);
//            if (!StringUtil.isNullOrEmpty(nickName)) {
//                member.setNickname(nickName);
//            } else {
//                member.setNickname(phone);
//            }
//            int count = authMemberMapper.insertSelective(member);
//            JSONObject data = new JSONObject();
//            if (count == 1) {
//                data.put("userId", guid);
//                return ResultUtil.buildSuccessResult(1000, data);
//            } else {
//                return ResultUtil.buildFailResult(2003, "用户注册失败");
//            }
//        } else {
//            return ResultUtil.buildFailResult(Integer.parseInt(result.getCode()), result.getMsg().toString());
//        }
//    }

    /**
     * 未用
     *
     * @param params
     * @return
     */
//    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
//    public JSONObject updateBvmsUserInfo(JSONObject params) {
//        JSONObject resultData = new JSONObject();
//        String password = params.getString("password");
//        String guid = params.getString("userId");
//        String token = params.getString("accessToken");
//        String phone = params.getString("phone");
//        String code = params.getString("smgCode");
//        String email = params.getString("email");//电子邮箱
//        String sex = params.getString("gender");//性别
//        String facePhotoPath = params.getString("imgUrl");//头像
//        String nickName = params.getString("nickName");//昵称
//        String filename = UUID.randomUUID().toString();
//
//        if (StringUtil.isNullOrEmpty(phone)) {
//            throw new BusinessException(ResultCodeEnum.ERR_41056, "请输入绑定的手机号");
//        }
//        if (StringUtil.isNullOrEmpty(password)) {
//            throw new BusinessException(ResultCodeEnum.ERR_41048, "密码不能为空");
//        }
//        if (StringUtil.isNullOrEmpty(token)) {
//            throw new BusinessException(ResultCodeEnum.ERR_41057, "token不能为空");
//        }
//
//        if (!StringUtil.isNullOrEmpty(facePhotoPath)) {
//
//            String path = config.getBlessImgPath().concat("/blessFacePhoto");
//
//            int flag = ReadFileUtil.uploadImage(facePhotoPath, filename, path);
//            if (flag != 1) {
//                throw new BusinessException(ResultCodeEnum.ERR_41051, "上传头像失败");
//            }
//        }
//        UserService userService = UserService.getInstance();
//        UserInfoParam userInfoParam = new UserInfoParam();
//        userInfoParam.setGuid(guid);
//        userInfoParam.setAccess_token(token);
//        ResultData userInfoGet = userService.userInfoGet(userInfoParam);
//        if (!"1000".equals(userInfoGet.getCode())) {
//            return ResultUtil.buildFailResult(Integer.parseInt(userInfoGet.getCode()), userInfoGet.getMsg());
//        }
//        JSONObject data = (JSONObject) userInfoGet.getData();
//        String bingPhone = data.getString("phone");
//        if (!phone.equals(bingPhone)) {
//            return ResultUtil.buildFailResult(4108, "请输入之前绑定的手机号");
//        }
//
//        RWrapper r = messageConsumer.verify(phone, code);
//        UserUpdateParam updateParams = new UserUpdateParam();
//        if ("1000".equals(r.getCode())) {//手机验证成功
//            updateParams.setAccess_token(token);
//            updateParams.setGuid(guid);
//            updateParams.setPassword(password);
//            updateParams.setPhone(phone);
//            ResultData updateResult = userService.userUpdate(updateParams);
//            if ("1000".equals(updateResult.getCode())) {
//                AuthMember member = new AuthMember();
//                member.setId(guid);
//                member.setPhone(phone);
//                if (!StringUtil.isNullOrEmpty(email)) {
//                    member.setEmail(email);
//                }
//                if (!StringUtil.isNullOrEmpty(facePhotoPath)) {
//
//                    member.setFacephotopath("/blessFacePhoto/" + filename + ".jpg");
//                }
//                if (!StringUtil.isNullOrEmpty(sex)) {
//                    member.setGender(sex);
//                }
//                if (!StringUtil.isNullOrEmpty(nickName)) {
//                    member.setNickname(nickName);
//                }
//                int count = authMemberMapper.updateByPrimaryKeySelective(member);
//                if (count == 1) {//用户基本信息修改成功
//                    AuthMember authMember = authMemberMapper.selectByPrimaryKey(guid);
//                    resultData.put("email", authMember.getEmail());
//                    resultData.put("facephotopath", authMember.getFacephotopath());
//                    resultData.put("gender", authMember.getGender());
//                    resultData.put("name", authMember.getName());
//                    resultData.put("nickName", authMember.getNickname());
//                    resultData.put("phone", authMember.getPhone());
//                }
//                return ResultUtil.buildSuccessResult(1000, resultData);
//            } else {
//                return ResultUtil.buildFailResult(Integer.parseInt(updateResult.getCode()), updateResult.getMsg());
//            }
//        } else {
//            return ResultUtil.buildFailResult(Integer.parseInt(r.getCode()), r.getMsg());
//        }
//    }

    /**
     * 手机号+验证码登录
     *
     * @param
     * @return
     */
/*    public JSONObject loginByCode(JSONObject params) {
        JSONObject resultData = new JSONObject();
        String phone = params.getString("phone");
        String code = params.getString("smgCode");

        if (StringUtil.isNullOrEmpty(phone)) {
            throw new BusinessException(ResultCodeEnum.ERR_41052, "手机号不能为空");
        }
        if (StringUtil.isNullOrEmpty(code)) {
            throw new BusinessException(ResultCodeEnum.ERR_41053, "验证码不能为空");
        }

        AuthMember member = authMemberMapper.getAuthMemberByPhone(phone);
        if (member != null) {
            //验证验证码是否正确
            UserService userService = UserService.getInstance();

            RWrapper r = messageConsumer.verify(phone, code);
            if (!"1000".equals(r.getCode())) {
                return ResultUtil.buildFailResult(2040, "请输入正确的验证码");
            }
            //获取token
            Map<String, Object> tokenGet = new LinkedHashMap<>();
            tokenGet.put("username", config.getBviModule().toUpperCase().concat("_").concat(phone));
            tokenGet.put("password", "");
            tokenGet.put("module", config.getBviModule());
            tokenGet.put("client_id", config.getBviClientId());
            tokenGet.put("client_secret", config.getBviClientSecert());
            tokenGet.put("grant_type", config.getBviGrantType());
            tokenGet.put("scope", config.getBviScope());
            tokenGet.put("login_type", config.getLoginTypeUp());
            tokenGet.put("roleid", config.getRoleId());
            tokenGet.put("loginByPhoneCode", "1");

            JSONObject resultGet = sendPost(config.getOauth_url() + "/oauth/token/get", tokenGet);
            if ("1000".equals(resultGet.getString("code"))) {
                JSONObject jsonData = resultGet.getJSONObject("data");
                jsonData.put("phone", phone);
                jsonData.put("guid", member.getId());
                jsonData.put("email", member.getEmail());
                if (!StringUtil.isNullOrEmpty(member.getFacephotopath())) {
                    if (member.getFacephotopath().contains("wx.qlogo.cn")) {
                        jsonData.put("facephotopath", member.getFacephotopath());
                    } else {
                        jsonData.put("facephotopath", config.getPicture_url() + member.getFacephotopath());
                    }
                }
                jsonData.put("gender", member.getGender());
                jsonData.put("name", member.getName());
                jsonData.put("nickName", member.getNickname());
                jsonData.put("phone", member.getPhone());
                jsonData.put("idCardNumber", member.getIdcardnumber());
                resultData.put("data", jsonData);
                resultData.put("phone", phone);
                log.info("登录成功，返回前台参数 ：" + jsonData);
                return ResultUtil.buildSuccessResult(Integer.parseInt(resultGet.getString("code")), resultData);
            }
            return ResultUtil.buildFailResult(Integer.parseInt(resultGet.getString("code")), resultGet.getString("msg"));
        }

        JSONObject registParams = new JSONObject();
        registParams.put("phone", phone);
        registParams.put("smsCode", code);
        registParams.put("password", "123456");

        JSONObject registResult = registerWithPhone(registParams);
        if ("1000".equals(registResult.getString("code"))) {
            JSONObject loginParams = new JSONObject();
            loginParams.put("phone", phone);
            loginParams.put("userName", phone);
            loginParams.put("password", "123456");
            return login(loginParams);
        }
        return ResultUtil.buildFailResult(Integer.parseInt(registResult.getString("code")), "账号登录失败，请稍后重试");
    }*/

    //获取国彩商户列表
    public List<AuthMember> getAuthMemberList(SearchBean searchBean) {
        int pageNum = (searchBean.getPageNum() - 1) * searchBean.getPageSize();
        searchBean.setPageNum(pageNum);
        List<AuthMember> authMemberList = this.authMemberMapper.getPageAll(searchBean);
        return authMemberList;
    }

    public Integer getAuthMemberAllCount(SearchBean searchBean) {
        return authMemberMapper.getAllCount(searchBean);
    }

    //获取企业商户列表
    public List<AuthCompany> getAuthCompanyList(SearchBean searchBean) {
        int pageNum = (searchBean.getPageNum() - 1) * searchBean.getPageSize();
        searchBean.setPageNum(pageNum);
        List<AuthCompany> authCompanyList = this.authCompanyMapper.getComPageAll(searchBean);
        return authCompanyList;
    }

    public int getAuthCompanyAllCount(SearchBean searchBean) {
        return authCompanyMapper.getComAllCount(searchBean);
    }

    public int getEmptyAll() {
        return randomMappingGroupMapper.selectEmptyAll();
    }

    /**
     * 带有终端设备信息的登录
     *
     * @param params
     * @return
     */
    public JSONObject loginWithTerminalInfo(JSONObject params) {

        String phone = params.getString("phone");
        String code = params.getString("smgCode");
        //判断它为注册后跳转登录
        String flag = params.getString("flag");

        //判断是哪个平台登录  31：福包天下安卓端 32：福包天下苹果端
        String clientID = params.getString("clientID");

        if (StringUtil.isNullOrEmpty(phone)) {
            throw new BusinessException(ResultCodeEnum.ERR_41052, "手机号不能为空");
        }
        if (StringUtil.isNullOrEmpty(code)) {
            throw new BusinessException(ResultCodeEnum.ERR_41053, "验证码不能为空");
        }
        if (StringUtil.isNullOrEmpty(clientID)) {
            throw new BusinessException(ResultCodeEnum.FAIL, "clientID不能为空");
        }
        AuthMember member;
        if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
            log.info("福包天下用户登录");
            //首先查看是否是空间主  如果是空间主 直接可以登录 如果不是空间主 是否是用户
            AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByPhone(phone);
            log.info("查询是否是空间主authSpaceMaster={}", authSpaceMaster);
            if (authSpaceMaster == null) {
                //8福包天下用户
                member = authMemberMapper.getAuthMemberByPhoneAndRole(phone, Integer.parseInt(CommonConfig.FBTX_ROLE_ID));
                log.info("查询是否是福包天下用户member={}", member);
            } else {
                //空间主登录
                log.info("这个用户是空间主直接登录");
                if (authSpaceMaster.getState().intValue() == 0) {
                    throw new BusinessException(ResultCodeEnum.FAIL, "账号被禁用");
                }
                return loginByPhoneSpaceMaster(params, authSpaceMaster);
            }
        } else {
            log.info("国彩福包用户登录");
            //member = authMemberMapper.getAuthMemberByPhone(phone);
            member = authMemberMapper.getNewAuthMemberByPhone(phone);
        }

        if (member != null) {
            log.info("用户存在{}", phone);
            log.info("状态为={}", member.getStatus());
            if (member.getStatus().intValue() == 0) {
                throw new BusinessException(ResultCodeEnum.FAIL, "账号被禁用");
            }
            return loginByPhone(params, member);
        }
        log.info("用户不存在，去注册{}", phone);
        JSONObject registParams = new JSONObject();
        registParams.put("phone", phone);
        registParams.put("smsCode", code);
        registParams.put("password", "123456");
        //通过clientID去判断用户
        registParams.put("clientID", clientID);

        JSONObject registResult = registerWithPhone(registParams);
        log.info("用户注册返回数据{}", registResult.toString());
        if ("1000".equals(registResult.getString("code"))) {
            params.put("flag", 1);
            return loginByPhone(params, null);
        }
        return ResultUtil.buildFailResult(Integer.parseInt(registResult.getString("code")), "账号登录失败，请稍后重试");
    }

    public int batchTransfer() {
        List<AuthMember> all = authMemberMapper.getAll();
        int count = 0;
        if (all.size() > 0) {
            for (AuthMember a : all) {
                JSONObject data = new JSONObject();
                if (!StringUtil.isNullOrEmpty(a.getId())) {
                    data.put("guid", a.getId());
                }
                if (!StringUtil.isNullOrEmpty(a.getNickname())) {
                    data.put("nickName", a.getNickname());
                }
                if (!StringUtil.isNullOrEmpty(a.getPhone())) {
                    data.put("phone", a.getPhone());
                }
                if (!StringUtil.isNullOrEmpty(a.getName())) {
                    data.put("userName", a.getName());
                }

                Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", data.toJSONString().getBytes());
                producer.send(message);
                count++;
            }
        }


        List<Company> companys = companyMapper.getAll(null, null, null);
        if (companys.size() > 0) {
            for (Company c : companys) {
                JSONObject data = new JSONObject();
                if (!StringUtil.isNullOrEmpty(c.getId())) {
                    data.put("guid", c.getId());
                }
                if (!StringUtil.isNullOrEmpty(c.getPhone())) {
                    data.put("phone", c.getPhone());
                }
                if (!StringUtil.isNullOrEmpty(c.getName())) {
                    data.put("userName", c.getName());
                }
                Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", data.toJSONString().getBytes());
                producer.send(message);
                count++;
            }
        }

        return 0;

    }

    public JSONObject wxLogin(JSONObject params) throws Exception {
        //UserService userService = UserService.getInstance();
        log.info("进入微信登录，参数为：" + params);
        String openId = params.getString("openId");
        String unionId = params.getString("unionid");
        String imei = params.getString("imei");
        String macAddr = params.getString("macAddr");
        String ip = params.getString("ip");
        String terminalType = params.getString("terminalType");
        String phoneModel = params.getString("phoneModel");
        String systemVersion = params.getString("systemVersion");
        String channel = params.getString("channel");
        String appVersion = params.getString("appVersion");
        String phone = params.getString("phone");

        if (StringUtil.isNullOrEmpty(openId) || StringUtil.isNullOrEmpty(unionId)) {
            throw new BusinessException("openId,unionid不能为空");
        }
        if (StringUtil.isNullOrEmpty(phone)) {//没有手机号，说明是微信登录
            String username = unionId;
            String password = unionId;
            UserTokenParam tokenParam = new UserTokenParam();
            tokenParam.setUsername(username);
            tokenParam.setPassword(password);
            tokenParam.setClient_id(config.getBviClientId());
            tokenParam.setClient_secret(config.getBviClientSecert());
            tokenParam.setGrant_type(config.getBviGrantType());
            tokenParam.setScope(config.getBviScope());
            tokenParam.setLogin_type(config.getLoginTypeOPENID());
            tokenParam.setModule(config.getBviModule());
            tokenParam.setRoleid(config.getRoleId());

            //新增字段
            tokenParam.setImei(imei);
            tokenParam.setMacAddr(macAddr);
            tokenParam.setIp(ip);
            tokenParam.setTerminalType(terminalType);
            tokenParam.setPhoneModel(phoneModel);
            tokenParam.setSystemVersion(systemVersion);
            tokenParam.setChannel(channel);
            tokenParam.setAppVersion(appVersion);
            tokenParam.setCheckImei(true);
            tokenParam.setLoginByPhoneCode("0");
            tokenParam.setLoginType(1);

            tokenParam.setUnionId(unionId);

            //ResultData result = userService.userTokenGet(tokenParam);

            MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
            postParameters.add("openId", openId);
            postParameters.add("imei", imei);
            postParameters.add("macAddr", macAddr);
            postParameters.add("ip", ip);
            postParameters.add("terminalType", terminalType);
            postParameters.add("phoneModel", phoneModel);
            postParameters.add("systemVersion", systemVersion);
            postParameters.add("channel", channel);
            postParameters.add("appVersion", appVersion);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/x-www-form-urlencoded");
            headers.add("authorization", AuthUtil.getBasicAuthHeader(config.getBviClientId(), config.getBviClientSecert()));
            HttpEntity<MultiValueMap<String, Object>> parameter = new HttpEntity<>(postParameters, headers);
            RWrapper<JSONObject> result = restTemplate.postForObject("http://" + "auth" + "/login/openId", parameter, RWrapper.class);


            String code = result.getCode();
            if ("1000".equals(code)) {//登录成功
                String resData = JSON.toJSONString(result.getData());
                JSONObject data = JSON.parseObject(resData);
                AuthMember member = authMemberMapper.getAuthMemberByWXOpenId(openId);
                if (member == null) {
                    return ResultUtil.buildFailResult(2020, "该用户不存在");
                }
                if (!StringUtil.isNullOrEmpty(member.getPhone())) {
                    data.put("phone", member.getPhone());
                }
                if (!StringUtil.isNullOrEmpty(member.getId())) {
                    data.put("guid", member.getId());
                }

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

                log.info("第三方登录成功，返回前台参数为：" + data);
                return ResultUtil.buildSuccessResult(Integer.parseInt(code), data);
            } else {
                return ResultUtil.buildFailResult(2020, "请绑定手机号！");
            }
        } else {//第一次微信登录 。即进行微信註冊

            return resisterByUnionId(params);
        }
    }

    private JSONObject resisterByUnionId(JSONObject params) throws Exception {
        //UserService userService = UserService.getInstance();
//		boolean flag = false;
        log.info("检查该手机号是否已绑定微信");

        String openId = params.getString("openId");
        String phone = params.getString("phone");
        String unionId = params.getString("unionid");
        Random random = new Random();
        int num = random.nextInt(899999) + 100000;
        String password = String.valueOf(num);
        AuthMember b = authMemberMapper.getAuthMemberByPhone(phone);
        if (b != null && !StringUtil.isNullOrEmpty(b.getWeixinopenid())) {
            String guid = b.getId();

            //將unionid与guid绑定

            params.remove("phone");
            return wxLogin(params);
//					return ResultUtil.buildFailResult(2003, "该手机号已被绑定！");
        }

        log.info("开始进行第三方注册");
        String nickName = "";
        if (!StringUtil.isNullOrEmpty(params.getString("nickName"))) {
            nickName = params.getString("nickName").replaceAll("[^\\u0000-\\uFFFF]", "*");//source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "*")
        }

        String facePhotoPath = params.getString("facePhotoPath");

        String sex = params.getString("sex");

        String smgCode = params.getString("smgCode");
        if (StringUtil.isNullOrEmpty(smgCode)) {
            throw new BusinessException("手机验证码不能为空");
        }
        /*UserRegParam registParam = new UserRegParam();
        registParam.setOpenid(openId);
        registParam.setPlatform(config.getPlatform());
        registParam.setPhone(phone);
        registParam.setPassword(password);
        registParam.setNickname(nickName);
        registParam.setIconUrl(facePhotoPath);
        registParam.setBackup(sex);
        registParam.setRoleid(config.getRoleId());
        registParam.setCode(smgCode);
        registParam.setUsername("BVI_" + phone);
        registParam.setRoleid(config.getRoleId());
        registParam.setUnionId(unionId);*/
        //ResultData RegistResult = userService.userReg(registParam);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("openid", openId);
        jsonObject.put("platform", config.getPlatform());
        jsonObject.put("phone", phone);
        jsonObject.put("password", password);
        jsonObject.put("nickName", nickName);
        jsonObject.put("iconUrl", facePhotoPath);
        jsonObject.put("backup", sex);
        jsonObject.put("roleid", config.getRoleId());
        jsonObject.put("code", smgCode);
        jsonObject.put("username", "BVI_" + phone);
        jsonObject.put("roleid", config.getRoleId());
        jsonObject.put("unionId", unionId);
        RWrapper<JSONObject> RegistResult = authFeignService.register(jsonObject);

        String RegistCode = RegistResult.getCode();
        if ("1000".equals(RegistCode)) {//注册成功
//					flag = true;
            log.info("第三方在4A平台注册成功 ,4A返回结果为：" + RegistResult);
            JSONObject returnResult = (JSONObject) RegistResult.getData();
            String guid = returnResult.getString("guid");
            log.info("创建账户以及member表");
            //Map<String, Object> insertParams = new LinkedHashMap<String, Object>();
            JSONObject insertParams = new JSONObject();
            insertParams.put("userId", guid);
            insertParams.put("userType", config.getUserType());
            insertParams.put("clientID", signClient.getClientId());
            insertParams.put("nonce_str", UUID.randomUUID().toString());
            String sign = Signature.getSign(insertParams, signClient.getClientSecret());
            insertParams.put("sign", sign);
            //JSONObject result = sendPost(config.getPayment_url() + config.getInsertAuthAccount(), insertParams);
            RWrapper<JSONObject> results = paymentFeignService.insertAuthAccount(insertParams);
            log.info("创建账户以及member表返回数据={}", results);
            JSONObject result = JSONObject.parseObject(results.toString());
            if (!"1000".equals(result.getString("code"))) {
                //回滚4A系统  删除用户
                log.info("账户创建不成功，原因为：" + result.getString("msg") + ",此时回滚4A平台，逻辑删除【" + returnResult.getString("username") + "】该用户");
                //ResultData deleteResult = userService.deleteByUserName(returnResult.getString("username"));
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("username", returnResult.getString("username"));
                RWrapper deleteResult = authFeignService.deleteByUserName(jsonObj);
                if (!"1000".equals(deleteResult.getCode())) {
                    JSONObject json = new JSONObject();
                    json.put("username", returnResult.getString("username"));
                    authFeignService.deleteByUserName(json);
                    //ResultData againResult = userService.deleteByUserName(returnResult.getString("username"));
                }
                throw new BusinessException("账户创建失败");
            }

            log.info("发送mq消息");

            JSONObject data = new JSONObject();
            data.put("guid", guid);
            data.put("nickName", nickName);
            data.put("phone", phone);
            Message message = new Message(MqInitializer.getTopic(), "userInfo", "bless", data.toJSONString().getBytes());
            producer.send(message);

            log.info("插入auth_member一条记录");
            AuthMember member = new AuthMember();
            member.setId(guid);
            member.setGuid(guid);
            member.setCreatetime(DateTimeUtils.getCurrentDateTime());
            member.setGender(sex);
            member.setPhone(phone);
            member.setFacephotopath(facePhotoPath);
            member.setNickname(nickName);
            member.setWeixinopenid(openId);
            int count = authMemberMapper.insertSelective(member);
            if (count != 1) {
                //ResultData deleteResult = userService.deleteByUserName(returnResult.getString("username"));
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("username", returnResult.getString("username"));
                RWrapper deleteResult = authFeignService.deleteByUserName(jsonObj);
                if (!"1000".equals(deleteResult.getCode())) {
                    //userService.deleteByUserName(returnResult.getString("username"));
                    JSONObject json = new JSONObject();
                    json.put("username", returnResult.getString("username"));
                    authFeignService.deleteByUserName(json);
                }
            }
            log.info("第三方注册成功，进入4A平台获取token");
            params.remove("phone");
            return wxLogin(params);
        } else {
            return ResultUtil.buildFailResult(Integer.parseInt(RegistCode), RegistResult.getMsg());
        }

    }

    private static AuthMemberMapper staticAuthMemberMapper;

    @PostConstruct
    public void initStaticAuthMemberMapper() {
        staticAuthMemberMapper = this.authMemberMapper;
    }

    public static AuthMemberMapper getStaticAuthMemberMapper() {
        return staticAuthMemberMapper;
    }

    public static void setStaticAuthMemberMapper(AuthMemberMapper staticAuthMemberMapper) {
        AuthService.staticAuthMemberMapper = staticAuthMemberMapper;
    }

    /**
     * 手机号验证码登录
     *
     * @param params
     * @param member
     * @return
     */
    private JSONObject loginByPhone(JSONObject params, AuthMember member) {
        log.info("进入手机号验证码登录入参={}", params);
        String imei = params.getString("imei");
        String macAddr = params.getString("macAddr");
        String ip = params.getString("ip");
        String terminalType = params.getString("terminalType");
        String phoneModel = params.getString("phoneModel");
        String systemVersion = params.getString("systemVersion");
        String channel = params.getString("channel");
        String appVersion = params.getString("appVersion");

        String phone = params.getString("phone");
        String code = params.getString("smgCode");
        //判断它为注册后跳转登录
        String flag = params.getString("flag");

        String clientID = params.getString("clientID");


        if (member == null) {
            if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                member = authMemberMapper.getAuthMemberByPhoneAndRole(phone, Integer.parseInt(CommonConfig.FBTX_ROLE_ID));
            } else {
                //member = authMemberMapper.getAuthMemberByPhone(phone);
                member = authMemberMapper.getNewAuthMemberByPhone(phone);
            }
        }
        log.info("member={}", member);
        String roleId = "";
        String userName = "";
        if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
            roleId = CommonConfig.FBTX_ROLE_ID;
            userName = CommonConfig.USERNAME_PREFIX.concat("_").concat(phone);
        } else {
            roleId = config.getRoleId();
            userName = config.getBviModule().toUpperCase().concat("_").concat(phone);
        }
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("userName", userName);
        postParameters.add("roleId", roleId);
        postParameters.add("mobile", phone);
        postParameters.add("code", code);
        postParameters.add("imei", imei);
        postParameters.add("ip", ip);
        postParameters.add("terminalType", terminalType);
        postParameters.add("macAddr", macAddr);
        postParameters.add("phoneModel", phoneModel);
        postParameters.add("systemVersion", systemVersion);
        postParameters.add("regFlag", flag);
        postParameters.add("channel", channel);
        postParameters.add("appVersion", appVersion);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("authorization", AuthUtil.getBasicAuthHeader(config.getBviClientId(), config.getBviClientSecert()));
        HttpEntity<MultiValueMap<String, Object>> parameter = new HttpEntity<>(postParameters, headers);
        RWrapper<Map<String, String>> result = restTemplate.postForObject("http://auth/login/mobile", parameter, RWrapper.class);
        log.info("调用auth项目登录返回数据={}", result);
        if ("1000".equals(result.getCode())) {
            JSONObject json = null;
            if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                //调用融云
                json = getRongCloudToken(member.getId(), member.getNickname(), member.getFacephotopath(), CommonConfig.FBTX_ROLE_ID);
                log.info("json={}", json);
            }
            Map<String, String> jsonData = result.getData();
            jsonData.put("guid", member.getId());
            jsonData.put("email", member.getEmail());
            if (!StringUtil.isNullOrEmpty(member.getFacephotopath())) {
                if (member.getFacephotopath().contains("http")) {
                    jsonData.put("facephotopath", member.getFacephotopath());
                } else {
                    jsonData.put("facephotopath", config.getPicture_url() + member.getFacephotopath());
                }
            }
            jsonData.put("gender", member.getGender());
            jsonData.put("name", member.getName());
            jsonData.put("nickName", member.getNickname());
            jsonData.put("phone", member.getPhone());
            jsonData.put("idCardNumber", member.getIdcardnumber());
            if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                jsonData.put("rongCloudToken", json.getString("token"));
                jsonData.put("rongCloudId", json.getString("id"));
            }
            JSONObject resultData = new JSONObject();
            resultData.put("data", jsonData);
            resultData.put("phone", phone);
            log.info("登录成功，返回前台参数 ：" + jsonData);
            return ResultUtil.buildSuccessResult(1000, resultData);
        }
        return ResultUtil.buildFailResult(Integer.parseInt(result.getCode()), result.getMsg());
    }


    /**
     * 手机号验证码登录(空间主登录)
     *
     * @param params
     * @param authSpaceMaster
     * @return
     */
    private JSONObject loginByPhoneSpaceMaster(JSONObject params, AuthSpaceMaster authSpaceMaster) {
        log.info("进入手机号验证码登录入参={}", params);
        String imei = params.getString("imei");
        String macAddr = params.getString("macAddr");
        String ip = params.getString("ip");
        String terminalType = params.getString("terminalType");
        String phoneModel = params.getString("phoneModel");
        String systemVersion = params.getString("systemVersion");
        String channel = params.getString("channel");
        String appVersion = params.getString("appVersion");

        String phone = params.getString("phone");
        String code = params.getString("smgCode");
        //判断它为注册后跳转登录
        String flag = params.getString("flag");

        //判断是哪个平台登录  31：福包天下安卓端 32：福包天下苹果端
        String clientID = params.getString("clientID");

        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("userName", CommonConfig.USERNAME_PREFIX.concat("_").concat(phone));
        postParameters.add("roleId", CommonConfig.FBTX_SPACE_ROLE_ID);
        postParameters.add("mobile", phone);
        postParameters.add("code", code);
        postParameters.add("imei", imei);
        postParameters.add("ip", ip);
        postParameters.add("terminalType", terminalType);
        postParameters.add("macAddr", macAddr);
        postParameters.add("phoneModel", phoneModel);
        postParameters.add("systemVersion", systemVersion);
        postParameters.add("regFlag", flag);
        postParameters.add("channel", channel);
        postParameters.add("appVersion", appVersion);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("authorization", AuthUtil.getBasicAuthHeader(config.getBviClientId(), config.getBviClientSecert()));
        HttpEntity<MultiValueMap<String, Object>> parameter = new HttpEntity<>(postParameters, headers);
        RWrapper<Map<String, String>> result = restTemplate.postForObject("http://auth/login/mobile", parameter, RWrapper.class);
        log.info("调用auth项目登录返回数据={}", result);
        if ("1000".equals(result.getCode())) {
            JSONObject json = null;
            if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                //调用融云
                json = getRongCloudToken(authSpaceMaster.getUserId(), authSpaceMaster.getNickname(), authSpaceMaster.getHeadPortrait(), CommonConfig.FBTX_SPACE_ROLE_ID);
                log.info("json={}", json);
            }
            Map<String, String> jsonData = result.getData();
            jsonData.put("guid", authSpaceMaster.getUserId());
            jsonData.put("email", authSpaceMaster.getEmail());
            String headPortrait = authSpaceMaster.getHeadPortrait();
            if (!headPortrait.contains("http")) {
                headPortrait = config.getPicture_url() + headPortrait;
            }
            jsonData.put("facephotopath", headPortrait);
            jsonData.put("gender", authSpaceMaster.getGender());
            jsonData.put("name", authSpaceMaster.getRealName());
            jsonData.put("nickName", authSpaceMaster.getNickname());
            jsonData.put("phone", authSpaceMaster.getPhone());
            jsonData.put("idCardNumber", authSpaceMaster.getIdcardNumber());
            if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
                jsonData.put("rongCloudToken", json.getString("token"));
                jsonData.put("rongCloudId", json.getString("id"));
            }
            JSONObject resultData = new JSONObject();
            resultData.put("data", jsonData);
            resultData.put("phone", phone);
            log.info("登录成功，返回前台参数 ：" + jsonData);
            return ResultUtil.buildSuccessResult(1000, resultData);
        }
        return ResultUtil.buildFailResult(Integer.parseInt(result.getCode()), result.getMsg());
    }


    /**
     * 验证支付密码是否正确
     *
     * @param jsonObject
     * @return
     */
    public String checkPaymentPassword(JSONObject jsonObject) {
        RWrapper<JSONObject> resData = paymentFeignService.checkPaymentPassword(jsonObject);
        log.info("验证支付密码是否正确返回参数={}", resData.toString());
        return resData.toString();
    }

    public String queryFingerPayStatus(JSONObject jsonObject) {
        RWrapper<JSONObject> resData = paymentFeignService.queryFingerPayStatus(jsonObject);
        log.info("查询设备指纹支付的状态返回值={}", resData.toString());
        return resData.toString();
    }

    public String setFingerPayPwd(JSONObject jsonObject) {
        RWrapper<JSONObject> resData = paymentFeignService.setFingerPayPwd(jsonObject);
        log.info("设置指纹支付的状态返回值={}", resData.toString());
        return resData.toString();
    }

    public String checkFingerPayPwd(JSONObject jsonObject) {
        RWrapper<JSONObject> resData = paymentFeignService.checkFingerPayPwd(jsonObject);
        log.info("检查指纹支付的状态返回值={}", resData.toString());
        return resData.toString();
    }

    /**
     * 获取融云token
     *
     * @param userId    用户guid
     * @param username  用户昵称
     * @param facePhoto 用户头像
     * @param owner     是否是空间主 0不是1是
     * @return
     */
    public JSONObject getRongCloudToken(String userId, String username, String facePhoto, String owner) {
        log.info("userId={}", userId);
        log.info("username={}", username);
        log.info("facePhoto={}", facePhoto);
        log.info("owner={}", owner);
        if (StringUtil.isNullOrEmpty(userId, username, facePhoto, owner)) {
            throw new BusinessException("获取融云token入参数为空!");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        jsonObject.put("username", username);
        jsonObject.put("portrait", facePhoto);
        jsonObject.put("owner", owner);
        log.info("开始调用融云参数={}", jsonObject);
        RWrapper<JSONObject> res = messageFeignService.getToken(jsonObject);
        log.info("结束调用融云返回数据={}", res);
        if (!"1000".equals(res.getCode())) {
            throw new BusinessException(res.getMsg());
        }
        String token = res.getData().getString("token");
        String id = res.getData().getString("id");
        if (StringUtil.isNullOrEmpty(token)) {
            throw new BusinessException("融云返回token为空！");
        }
        if (StringUtil.isNullOrEmpty(id)) {
            throw new BusinessException("融云返回id为空！");
        }
        return res.getData();
    }
}

