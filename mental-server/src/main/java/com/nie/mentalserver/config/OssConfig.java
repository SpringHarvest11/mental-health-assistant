package com.nie.mentalserver.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.comm.Protocol;
import com.nie.mental.common.properties.OssProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssConfig {

    // OSS 客户端线程安全，全局单例；容器关闭时自动 shutdown
    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(OssProperties properties) {
        // 统一走 HTTPS，签名 URL 不会因为 endpoint 没写协议而退化成 http
        ClientBuilderConfiguration config = new ClientBuilderConfiguration();
        config.setProtocol(Protocol.HTTPS);
        return new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret(),
                config);
    }
}
