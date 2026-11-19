package com.nie.mentalserver.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PolicyConditions;
import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.properties.OssProperties;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mentalpojo.dto.FileUploadConfirmDTO;
import com.nie.mentalpojo.dto.FileUploadPolicyDTO;
import com.nie.mentalpojo.entity.SysFileInfo;
import com.nie.mentalpojo.vo.FileUploadVO;
import com.nie.mentalpojo.vo.UploadPolicyVO;

import com.nie.mentalserver.mapper.SysFileInfoMapper;
import com.nie.mentalserver.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final String URL_PREFIX = "/upload/";

    private static final Set<String> ALLOWED_EXT =
            Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    // Content-Type 由后端按扩展名决定，同时写入 policy 条件
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp",
            "bmp", "image/bmp");

    private static final Pattern FOLDER_PATTERN =
            Pattern.compile("^[a-z0-9_-]{1,50}/[a-z0-9_-]{1,50}$");

    // article/cover/yyyy/MM/32位uuid.ext
    private static final Pattern KEY_PATTERN =
            Pattern.compile("^[a-z0-9_-]{1,50}/[a-z0-9_-]{1,50}/\\d{4}/\\d{2}/[0-9a-f]{32}\\.(jpg|jpeg|png|gif|webp|bmp)$");

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssProperties ossProperties;

    @Autowired
    private SysFileInfoMapper sysFileInfoMapper;

    @Override
    public UploadPolicyVO createUploadPolicy(FileUploadPolicyDTO dto) {
        String folder = buildFolder(dto.getBusinessType(), dto.getBusinessField());
        requireText(dto.getBusinessId(), "businessId", 255);

        String ext = extractExt(dto.getFileName());
        String contentType = CONTENT_TYPES.get(ext);

        LocalDate today = LocalDate.now();
        String key = String.format("%s/%d/%02d/%s.%s",
                folder, today.getYear(), today.getMonthValue(),
                UUID.randomUUID().toString().replace("-", ""), ext);

        // 限定 key 前缀、5MB 上限、固定 Content-Type，防止凭证被挪作他用
        PolicyConditions conditions = new PolicyConditions();
        conditions.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, folder + "/");
        conditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 1L, ossProperties.getMaxSize());
        conditions.addConditionItem(MatchMode.Exact, PolicyConditions.COND_CONTENT_TYPE, contentType);

        Date expiration = new Date(System.currentTimeMillis() + ossProperties.getPolicyExpireSeconds() * 1000L);
        // generatePostPolicy 返回的是原始 JSON，表单要求 Base64；
        // calculatePostSignature 内部会对 JSON 做 Base64 后再签名，传入原始 JSON 即可
        String policyJson = ossClient.generatePostPolicy(expiration, conditions);
        String policy = Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
        String signature = ossClient.calculatePostSignature(policyJson);

        return UploadPolicyVO.builder()
                .host(buildOssHost())
                .key(key)
                .policy(policy)
                .signature(signature)
                .accessKeyId(ossProperties.getAccessKeyId())
                .contentType(contentType)
                .build();
    }

    @Override
    public FileUploadVO confirmUpload(FileUploadConfirmDTO dto) {
        requireText(dto.getKey(), "key", 255);
        String folder = buildFolder(dto.getBusinessType(), dto.getBusinessField());
        requireText(dto.getBusinessId(), "businessId", 255);

        String key = dto.getKey();
        if (!KEY_PATTERN.matcher(key).matches()) {
            throw new BusinessException("非法的文件 key");
        }
        if (!key.startsWith(folder + "/")) {
            throw new BusinessException("文件 key 与业务类型不匹配");
        }

        long size;
        try {
            ObjectMetadata metadata = ossClient.headObject(ossProperties.getBucketName(), key);
            size = metadata.getContentLength();
        } catch (Exception e) {
            log.warn("OSS HEAD 失败, key={}", key, e);
            throw new BusinessException("文件尚未上传成功");
        }
        if (size <= 0 || size > ossProperties.getMaxSize()) {
            deleteQuietly(key);
            throw new BusinessException("文件大小不符合要求");
        }

        String originalName = sanitizeOriginalName(dto.getOriginalName());
        Integer userId = CurrentHolder.getUserId();
        SysFileInfo info = SysFileInfo.builder()
                .originalName(originalName)
                .filePath(key)
                .fileSize(size)
                .fileType("IMG")
                .businessType(dto.getBusinessType())
                .businessId(dto.getBusinessId())
                .businessField(dto.getBusinessField())
                .uploadUserId(userId == null ? null : userId.longValue())
                .isTemp(0)
                .status(1)
                .build();
        try {
            sysFileInfoMapper.insert(info);
        } catch (RuntimeException e) {
            // 台账落库失败就回删对象，避免产生孤儿文件
            deleteQuietly(key);
            throw e;
        }
        return new FileUploadVO(toDisplayPath(key));
    }

    @Override
    public String resolveUrl(String path) {
        String key = path;
        if (key != null && key.startsWith(URL_PREFIX)) {
            key = key.substring(URL_PREFIX.length());
        }
        if (!StringUtils.hasText(key) || !KEY_PATTERN.matcher(key).matches()) {
            throw new BusinessException("非法的文件路径");
        }
        if (StringUtils.hasText(ossProperties.getCdnDomain())) {
            return toDisplayPath(key);
        }
        Date expiration = new Date(System.currentTimeMillis()
                + ossProperties.getSignedGetExpireSeconds() * 1000L);
        return ossClient.generatePresignedUrl(
                ossProperties.getBucketName(), key, expiration).toString();
    }

    private String buildFolder(String businessType, String businessField) {
        requireText(businessType, "businessType", 50);
        requireText(businessField, "businessField", 50);
        String folder = businessType.toLowerCase(Locale.ROOT) + "/" + businessField.toLowerCase(Locale.ROOT);
        if (!FOLDER_PATTERN.matcher(folder).matches()) {
            throw new BusinessException("businessType/businessField 仅支持字母、数字、下划线和中划线");
        }
        return folder;
    }

    private String extractExt(String fileName) {
        String name = StringUtils.getFilename(fileName);
        String ext = name == null ? null : StringUtils.getFilenameExtension(name);
        if (!StringUtils.hasText(ext)) {
            throw new BusinessException("文件名缺少扩展名");
        }
        ext = ext.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("仅支持 jpg、jpeg、png、gif、webp、bmp 格式的图片");
        }
        return ext;
    }

    private String sanitizeOriginalName(String originalName) {
        String name = StringUtils.getFilename(originalName);
        if (!StringUtils.hasText(name)) {
            return "cover";
        }
        return name.length() > 255 ? name.substring(0, 255) : name;
    }

    private String buildOssHost() {
        String endpoint = ossProperties.getEndpoint();
        String scheme = endpoint.startsWith("http://") ? "http://" : "https://";
        String host = endpoint.replaceFirst("^https?://", "");
        return scheme + ossProperties.getBucketName() + "." + host;
    }

    private String toDisplayPath(String key) {
        String cdnDomain = ossProperties.getCdnDomain();
        if (StringUtils.hasText(cdnDomain)) {
            String base = cdnDomain.endsWith("/")
                    ? cdnDomain.substring(0, cdnDomain.length() - 1)
                    : cdnDomain;
            return base + "/" + key;
        }
        return URL_PREFIX + key;
    }

    private void deleteQuietly(String key) {
        try {
            ossClient.deleteObject(ossProperties.getBucketName(), key);
        } catch (Exception e) {
            log.warn("OSS 回删失败, key={}", key, e);
        }
    }

    private void requireText(String value, String field, int maxLength) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(field + " 不能为空");
        }
        if (value.length() > maxLength) {
            throw new BusinessException(field + " 长度不能超过 " + maxLength);
        }
    }
}
