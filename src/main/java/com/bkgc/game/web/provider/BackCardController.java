package com.bkgc.game.web.provider;

import com.alibaba.fastjson.JSONObject;
import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.StringUtil;
import com.bkgc.common.utils.WrapperUtil;
import com.bkgc.game.service.BackCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouyuzhao
 * @date 2018/9/5
 */
@RestController
@RequestMapping("/backCard")
@Slf4j
public class BackCardController {

    @Autowired
    private BackCardService backCardService;


    /**
     * 根据卡包id修改卡的状态和过期时间
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/updateCardStatus")
    public RWrapper<JSONObject> updateCardStatus(@RequestBody JSONObject jsonObject) {
        log.info("根据卡包id修改卡的状态和过期时间入参={}", jsonObject);
        String cardId = jsonObject.getString("cardId");
        String userId = jsonObject.getString("userId");
        if (StringUtil.isNullOrEmpty(cardId)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "卡包ID不能为空!");
        }
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "用户ID不能为空!");
        }
        backCardService.updateCardStatus(cardId, userId);
        return WrapperUtil.ok();
    }


    /**
     * 退款的时候因子表的购彩金额减去
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/updateFactor")
    public RWrapper<JSONObject> updateFactor(@RequestBody JSONObject jsonObject) {
        log.info("退款的时候因子表的购彩金额减去入参={}", jsonObject);
        String userId = jsonObject.getString("userId");
        String refundAmount = jsonObject.getString("refundAmount");
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "用户ID不能为空!");
        }
        if (StringUtil.isNullOrEmpty(refundAmount)) {
            throw new BusinessException(ResultCodeEnum.PARAM_MISS, "退款金额不能为空!");
        }
        backCardService.updateFactor(userId,refundAmount);
        return WrapperUtil.ok();
    }
}
