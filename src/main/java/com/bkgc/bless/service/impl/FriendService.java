package com.bkgc.bless.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.user.AuthMember;
import com.bkgc.bean.user.AuthSpaceMaster;
import com.bkgc.bless.config.Config;
import com.bkgc.bless.constant.CommonConfig;
import com.bkgc.bless.consumer.MessageFeignService;
import com.bkgc.bless.mapper.AuthMemberMapper;
import com.bkgc.bless.mapper.AuthSpaceMasterMapper;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhouyuzhao
 */
@Slf4j
@Service
public class FriendService {


    @Autowired
    private AuthMemberMapper authMemberMapper;

    @Autowired
    private MessageFeignService messageFeignService;

    @Resource
    private Config config;

    @Autowired
    private AuthSpaceMasterMapper authSpaceMasterMapper;


    /**
     * 查询用户
     *
     * @param param
     */
    public JSONArray queryUser(String param, String pageNum) {
        PageHelper.startPage(Integer.parseInt(pageNum), 10);
        List<AuthMember> authMemberList = authMemberMapper.searchUser(param);
        log.info("authMemberList={}", authMemberList);
        JSONArray jsonArray = new JSONArray();
        if (!authMemberList.isEmpty()) {
            for (AuthMember authMember : authMemberList) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", authMember.getGuid());
                RWrapper<JSONObject> res = messageFeignService.getUserInfoByUserId(jsonObject);
                log.info("去用userId={},获取信息={}", authMember.getGuid(), res);
                String rcId = null;
                if ("1000".equals(res.getCode())) {
                    JSONObject json = res.getData();
                    if (json != null) {
                        rcId = json.getString("rcId");
                    }
                }
                JSONObject json = new JSONObject();
                json.put("userId", authMember.getGuid());
                json.put("nickname", authMember.getNickname());
                json.put("roleId", authMember.getRoleId());
                json.put("title", authMember.getTitle());
                String face = authMember.getFacephotopath();
                if (!StringUtil.isNullOrEmpty(face)) {
                    if (face.contains("http")) {
                        json.put("photoPath", face);
                    } else {
                        json.put("photoPath", config.getPicture_url() + face);
                    }
                }
                json.put("rcId", rcId);
                jsonArray.add(json);
            }
        }
        return jsonArray;
    }


    /**
     * 根据userId获取用户信息(供其他服务使用)
     *
     * @param userId
     */
    public JSONObject getUserInfo(String userId) {
        JSONObject json = new JSONObject();
        AuthSpaceMaster authSpaceMaster = authSpaceMasterMapper.selectByUserId(userId);
        log.info("空间主authSpaceMaster={}", authSpaceMaster);
        if (authSpaceMaster != null) {
            json.put("userId", authSpaceMaster.getUserId());
            json.put("realName", authSpaceMaster.getRealName() == null ? "" : authSpaceMaster.getRealName());
            json.put("nickName", authSpaceMaster.getNickname());
            json.put("phone", authSpaceMaster.getPhone());
            String photoPath = authSpaceMaster.getHeadPortrait();
            if (!StringUtil.isNullOrEmpty(photoPath)) {
                if (!photoPath.contains("http")) {
                    photoPath = config.getPicture_url() + photoPath;
                }
            }
            json.put("photoPath", photoPath);
            json.put("title", authSpaceMaster.getTitle());
            json.put("gender", authSpaceMaster.getGender());
            json.put("idCard", authSpaceMaster.getIdcardNumber());
            json.put("status", authSpaceMaster.getState());
            json.put("weiXinNum", authSpaceMaster.getWeixinNum());
            json.put("address", authSpaceMaster.getAddress());
            json.put("email", authSpaceMaster.getEmail());
            json.put("registerTime", authSpaceMaster.getRegisterTime());
            json.put("registerChannel", authSpaceMaster.getRegisterChannel());

        } else {
            AuthMember authMember = authMemberMapper.selectByguid(userId);
            if (authMember == null) {
                throw new BusinessException(ResultCodeEnum.USER_NOTEXIST, "用户不存在!");
            }
            json.put("userId", authMember.getId());
            json.put("realName", authMember.getName() == null ? "" : authMember.getName());
            json.put("nickName", authMember.getNickname());
            json.put("phone", authMember.getPhone());
            String photoPath = authMember.getFacephotopath();
            if (!StringUtil.isNullOrEmpty(photoPath)) {
                if (!photoPath.contains("http")) {
                    photoPath = config.getPicture_url() + photoPath;
                }
            }
            json.put("photoPath", photoPath);
            json.put("title", authMember.getTitle());
            json.put("gender", authMember.getGender());
            json.put("idCard", authMember.getIdcardnumber());
            json.put("status", authMember.getStatus());
            json.put("weiXinNum", authMember.getWeixinNum());
            json.put("address", authMember.getAddress());
            json.put("email", authMember.getEmail());
            json.put("registerTime", authMember.getCreatetime());
        }
        return json;
    }


    /**
     * 根据userId获取修改title
     *
     * @param userId
     */
    public void updateTitle(String userId) {
        authMemberMapper.updateTitle(userId, CommonConfig.TITLE_2);
    }
}
