package com.bkgc.bless.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.bean.bless.FeedBack;
import com.bkgc.bless.config.Config;
import com.bkgc.bless.constant.CommonConfig;
import com.bkgc.bless.mapper.FeedbackMapper;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.ReadFileUtil;
import com.bkgc.common.utils.ResultUtil;
import com.bkgc.common.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.UUID;

@Transactional
@Service
public class FeedbackService {
    @Autowired
    private FeedbackMapper feedbackMapper;

    @Resource
    private Config config;

    public JSONObject insertFeedback(JSONObject params) {
        String userId = params.getString("userId");
        String description = params.getString("description");
        String imgs = params.getString("imgs");
        String phone = params.getString("phone");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.ERR_41084, "用户ID不能为空");
        }
        if (StringUtil.isNullOrEmpty(phone)) {
            throw new BusinessException(ResultCodeEnum.ERR_41052, "手机号不能为空");
        }
        if (StringUtil.isNullOrEmpty(description)) {
            throw new BusinessException(ResultCodeEnum.ERR_41085, "问题和意见不能为空");
        }

        String clientID = params.getString("clientID");
        if (clientID.equals(CommonConfig.ANDROID_CLIENT_ID) || clientID.equals(CommonConfig.IOS_CLIENT_ID)) {
            description = "福包天下:" + description;
        }


        FeedBack feedBack = new FeedBack();
        String imgPath = "";
        if (!StringUtil.isNullOrEmpty(imgs)) {
            String[] imgPaths = imgs.split("_");
            //for (String feedBackpath : imgPaths) {
            for (int i = 0; i < imgPaths.length; i++) {
                String filename = UUID.randomUUID().toString();
                String feedBackpath = imgPaths[i];
                String path = config.getBlessImgPath().concat("/feedback/").concat(userId);
                //String path = config.getPicture_url() + "/feedback/".concat(userId);

                int flag = ReadFileUtil.uploadImage(feedBackpath, filename, path);
                if (flag != 1) {
                    throw new BusinessException(ResultCodeEnum.ERR_41086, "上传反馈信息图片失败");
                }
                String imgName = config.getPicture_url()+"/feedback/".concat(userId) + "/" + filename + ".jpg";

                imgPath += imgName + "|";
            }
        }
        if (!StringUtil.isNullOrEmpty(imgPath)) {
            imgPath = imgPath.substring(0, imgPath.length() - 1);
        }
        feedBack.setUserId(userId);
        feedBack.setDescription(description);
        feedBack.setImgs(imgPath);
        feedBack.setPhone(phone);
        feedbackMapper.insert(feedBack);
        JSONObject data = new JSONObject();
        data.put("result", "用户反馈成功");
        return ResultUtil.buildSuccessResult(1000, data);
    }
}
