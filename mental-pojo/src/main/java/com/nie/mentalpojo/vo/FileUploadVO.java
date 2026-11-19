package com.nie.mentalpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileUploadVO {

    // 相对路径，前端拼接 fileBaseUrl 展示
    private String filePath;
}
