package com.nie.mentalserver.service;

import com.nie.mental.common.result.PageResult;
import com.nie.mentalpojo.dto.ArticlePageQueryDTO;
import com.nie.mentalpojo.vo.KnowledgeArticlePageVO;
import com.nie.mentalpojo.vo.KnowledgeCategoryTreeVO;

import java.util.List;

public interface KnowledgeService {

    /**
     * 文章分类树
     * @return
     */
    List<KnowledgeCategoryTreeVO> categoryTree();

    /**
     * 文章分页
     * @param query
     * @return
     */
    PageResult<KnowledgeArticlePageVO> pageArticles(Integer userType, ArticlePageQueryDTO query);
}
