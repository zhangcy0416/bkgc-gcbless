package com.bkgc.bless.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Data
@Configuration
@ConfigurationProperties(prefix = SignClient.CONFIG_PREFIX)
public class SignClient {
    public final static String CONFIG_PREFIX = "sign";

    /**
     * 签名的clientId
     */
    @NotNull
    private String clientId;

    /**
     * 签名的clientSecret
     */
    @NotNull
    private String clientSecret;
}
