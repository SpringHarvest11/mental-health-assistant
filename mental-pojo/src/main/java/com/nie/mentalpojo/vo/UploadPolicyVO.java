package com.nie.mentalpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadPolicyVO {

    // OSS 上传地址（bucket 域名，不是 CDN）
    private String host;

    // 后端生成的对象 key
    private String key;

    private String policy;

    private String signature;

    private String accessKeyId;

    // 与 policy 中 Content-Type 条件一致，前端提交时必须原样携带
    private String contentType;
}
