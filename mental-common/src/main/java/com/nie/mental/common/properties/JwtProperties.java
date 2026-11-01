package com.nie.mental.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * jwt令牌加密密钥和过期时间
 */
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    //加密密钥
    private String secret;

    //过期时间
    private long expiration;
}