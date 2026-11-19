package com.nie.mentalpojo.dto;

import lombok.Data;

@Data
public class FileUploadConfirmDTO {

    private String key;

    private String originalName;

    private String businessType;

    private String businessId;

    private String businessField;
}
