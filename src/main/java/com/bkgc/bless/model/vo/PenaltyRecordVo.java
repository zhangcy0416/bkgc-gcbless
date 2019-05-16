package com.bkgc.bless.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author zhouyuzhao
 * @date 2018/12/6
 */
@Data
public class PenaltyRecordVo {

    /**
     * 用户的userID
     */
    private String userId;

    /**
     * 用户的真实姓名
     */
    private String realName;

    /**
     * 用户的昵称
     */
    private String nickName;

    /**
     * 用户的手机号
     */
    private String phone;

    /**
     * 用户现状态
     */
    private String status;

    /**
     * 被罚的类型(0：被禁用 2：被禁言）
     */
    private Integer type;

    /**
     * 被罚时间
     */
    private Date createTime;

}
