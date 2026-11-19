package com.nie.mentalpojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KnowledgeArticleDetailVO {

    //文章 ID
    private String id;

    //标题
    private String title;

    //摘要
    private String summary;

    //正文 HTML
    private String content;

    //封面相对路径
    private String coverImage;

    //分类id
    private Integer categoryId;

    //分类名称
    private String categoryName;

    //作者名字
    private String authorName;

    //阅读量
    private Integer readCount;

    //文章状态 0/1/2
    private Integer status;

    //逗号分隔标签，编辑回显用
    private String tags;

    //标签数组，前台展示用
    private List<String> tagArray;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;
}
