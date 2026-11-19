package com.nie.mentalpojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArticleUpdateDTO {

    //前端会随表单提交，实际以路径参数为准
    @Size(max = 36, message = "文章ID最多36个字符")
    private String id;

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题最多200个字符")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    @Size(max = 5000, message = "文章内容最多5000个字符")
    private String content;

    @Size(max = 500, message = "封面地址最多500个字符")
    private String coverImage;

    @NotNull(message = "请选择分类")
    private Integer categoryId;

    @Size(max = 1000, message = "文章摘要最多1000个字符")
    private String summary;

    @Size(max = 500, message = "标签最多500个字符")
    private String tags;
}
