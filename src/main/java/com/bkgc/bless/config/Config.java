package com.bkgc.bless.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Data
@Configuration
@ConfigurationProperties(prefix = Config.CONFIG_PREFIX)
public class Config {
    public final static String CONFIG_PREFIX = "config";

    @NotNull
    private String bless_url;
    @NotNull
    private String BlessImgPath;
    @NotNull
    private String bviClientId;
    @NotNull
    private String bviClientSecert;
    @NotNull
    private String bviModule;
    @NotNull
    private String bviGrantType;
    @NotNull
    private String bviScope;
    @NotNull
    private String loginTypeUp;
    @NotNull
    private String loginTypeOPENID;
    @NotNull
    private String platform;
    @NotNull
    private String roleId;
    @NotNull
    private String userType;
    @NotNull
    private String picture_url;
    //    @NotNull
//    private String BlessEnvelopeByWechat;
//    @NotNull
//    private String BlessEnvelopeByApp;
    @NotNull
    private Integer delayTime;
    @NotNull
    private String superAdmin;
    @NotNull
    private String iosFlag;
    private String gameAmount;
    private String loginType;
    private String loginByPhoneCode;
    @NotNull
    private String getEnvelopeByApp;
    @NotNull
    private String getEnvelopeByWechat;
    @NotNull
    private String errorBlessPage;
    @NotNull
    private String randNumSign;
    private String spreadGroupId;
    private String loginTypeUNIONID;
    @NotNull
    private Integer qrExpireMinutes;

    @NotNull
    private Integer isTestUserOn;

    @NotNull
    private Integer workerId;

}
