package com.bkgc.bless.model.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 被罚记录
 *
 * @author zhouyuzhao
 * @date 2018/12/6
 */
@Data
public class PenaltyRecord implements Serializable {
    /**
     * id
     */
    private Integer id;

    /**
     * 用户的userID
     */
    private String userId;

    /**
     * 被罚的类型(0：被禁用 2：被禁言）
     */
    private Integer type;

    /**
     * 角色(8：普通用户 9：空间主）
     */
    private Integer roleId;

    /**
     * 被罚时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

}
