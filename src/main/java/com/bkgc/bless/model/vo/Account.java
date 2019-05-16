package com.bkgc.bless.model.vo;

import com.bkgc.bean.account.AuthAccount;
import lombok.Data;

import java.util.Date;

/**
 * Created by yanqiang on 2017/11/13.
 */
@Data
public class Account extends AuthAccount {

    private int pageStart;

    private int pageEnd;

    private String name;

    private String nickname;

    private String gender;

    private String phone;

    private Date createTime;

    private String createTimeStr;

    private String address;

}
