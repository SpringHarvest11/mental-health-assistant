package com.nie.mentalserver.controller;

import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.Result;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mentalpojo.dto.FileUploadConfirmDTO;
import com.nie.mentalpojo.dto.FileUploadPolicyDTO;
import com.nie.mentalpojo.vo.FileUploadVO;
import com.nie.mentalpojo.vo.UploadPolicyVO;
import com.nie.mentalserver.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file")
@Tag(name = "文件上传")
public class CommonUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @Operation(summary = "获取 OSS 直传凭证")
    @PostMapping("/upload/policy")
    public Result<UploadPolicyVO> policy(@RequestBody FileUploadPolicyDTO dto) {
        requireAdmin();
        return Result.success(fileStorageService.createUploadPolicy(dto));
    }

    @Operation(summary = "确认上传完成")
    @PostMapping("/upload/confirm")
    public Result<FileUploadVO> confirm(@RequestBody FileUploadConfirmDTO dto) {
        requireAdmin();
        return Result.success(fileStorageService.confirmUpload(dto));
    }

    private void requireAdmin() {
        if (!Integer.valueOf(2).equals(CurrentHolder.getUserType())) {
            throw new BusinessException("无权限上传文件");
        }
    }
}
