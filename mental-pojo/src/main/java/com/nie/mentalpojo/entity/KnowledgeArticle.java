package com.nie.mentalpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class KnowledgeArticle {

    //文章ID(UUID)
    private String id;

    //分类id
    private Integer categoryId;

    //文章标题
    private String title;

    //文章摘要
    private String summary;

    //文章内容
    private String content;

    //文章封面
    private String coverImage;

    //文章标签
    private String tags;

    //作者id
    private Integer authorId;

    //阅读量
    private Integer readCount;

    //状态 1:已发布 0：草稿 2：
    private Integer status;

    //发布时间
    private LocalDateTime publishedAt;

    //创建时间
    private LocalDateTime createdAt;

    //修改时间
    private LocalDateTime updatedAt;
}
