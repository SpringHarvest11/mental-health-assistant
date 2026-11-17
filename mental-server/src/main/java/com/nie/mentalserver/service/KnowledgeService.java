package com.nie.mentalserver.service;

import com.nie.mentalpojo.vo.KnowledgeCategoryTreeVO;

import java.util.List;

public interface KnowledgeService {

    /**
     * 文章分类树
     * @return
     */
    List<KnowledgeCategoryTreeVO> categoryTree();
}
