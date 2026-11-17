package com.nie.mentalpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KnowledgeCategoryTreeVO {

    //分类id
    private Integer id;

    //分类名称
    private String categoryName;

    //子分类
    private List<KnowledgeCategoryTreeVO> children;
}
