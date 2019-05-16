package com.bkgc.bless.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Data
@Configuration
@ConfigurationProperties(prefix = WxConfig.WX_PREFIX)
public class WxConfig {

    public final static String WX_PREFIX = "wx";

    @NotNull
    private String appid;
    @NotNull
    private String secret;
    @NotNull
    private String wxGetToken;
    @NotNull
    private String tokenGrantType;
    @NotNull
    private String getInfoUrl;
    @NotNull
    private String lang;

}
