package com.nie.mentalpojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KnowledgeArticlePageVO {

    //文章 ID
    private String id;

    //标题
    private String title;

    //封面绝对路径
    private String coverImage;

    //分类id
    private Integer categoryId;

    //分类名称
    private String categoryName;

    //作者名字
    private String authorName;

    //阅读量
    private Integer readCount;

    //文章状态 0/1/2，必须是数字
    private Integer status;

    //更新时间（兼容"发布时间"展示）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    //发布时间，支持按此字段排序
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;
}
