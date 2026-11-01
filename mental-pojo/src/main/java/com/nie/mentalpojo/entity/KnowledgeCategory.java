package com.nie.mentalpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识文章分类表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeCategory {

    //分类id
    private Integer id;

    //父类id
    private Integer parentId;

    //分类名称
    private String categoryName;

    //分类代码
    private String categoryCode;

    //分类描述
    private String description;

    //分类排序
    private Integer sortOrder;

    //分类状态 0：禁用；1：启用
    private Integer status;

    //分类创建时间
    private LocalDateTime createdAt;

    //分类最后修改时间
    private LocalDateTime updatedAt;
}
