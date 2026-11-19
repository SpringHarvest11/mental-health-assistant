package com.nie.mentalpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统文件信息表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysFileInfo {

    //文件ID
    private Long id;

    //原始文件名（用户上传时的文件名）
    private String originalName;

    //文件访问路径（服务器存储路径）
    private String filePath;

    //文件大小，单位：字节
    private Long fileSize;

    //文件类型（IMG/PDF/TXT/DOC/XLS等）
    private String fileType;

    //业务类型（用于区分文件用途，如：avatar/document/attachment）
    private String businessType;

    //业务对象ID（关联的业务数据主键）
    private String businessId;

    //业务字段名（对应业务表中的字段名）
    private String businessField;

    //上传用户ID（记录谁上传的文件）
    private Long uploadUserId;

    //是否临时文件（0:否 1:是）
    private Integer isTemp;

    //状态（0:删除 1:正常）
    private Integer status;

    //创建时间
    private LocalDateTime createTime;

    //过期时间（仅对临时文件有效）
    private LocalDateTime expireTime;

}
