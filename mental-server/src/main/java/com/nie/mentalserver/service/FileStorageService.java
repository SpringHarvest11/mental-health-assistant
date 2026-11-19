package com.nie.mentalserver.service;

import com.nie.mentalpojo.dto.FileUploadConfirmDTO;
import com.nie.mentalpojo.dto.FileUploadPolicyDTO;
import com.nie.mentalpojo.vo.FileUploadVO;
import com.nie.mentalpojo.vo.UploadPolicyVO;

public interface FileStorageService {

    /**
     * 生成 OSS PostObject 直传凭证
     */
    UploadPolicyVO createUploadPolicy(FileUploadPolicyDTO dto);

    /**
     * 前端直传成功后确认，校验对象并写入 sys_file_info，返回可展示的地址
     */
    FileUploadVO confirmUpload(FileUploadConfirmDTO dto);

    /**
     * 把 /upload/... 相对路径解析为 CDN 地址或 OSS 签名地址
     */
    String resolveUrl(String path);
}
