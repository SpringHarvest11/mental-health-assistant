package com.nie.mental.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    private String endpoint;

    private String accessKeyId;

    private String accessKeySecret;

    private String bucketName;

    // CDN 加速域名；为空时回退到后端 /upload/** 302 到签名 URL
    private String cdnDomain;

    // PostObject 直传凭证有效期
    private long policyExpireSeconds = 300;

    // 单文件大小上限（字节）
    private long maxSize = 5L * 1024 * 1024;

    // 无 CDN 时签名 GET 链接的有效期
    private long signedGetExpireSeconds = 600;
}

