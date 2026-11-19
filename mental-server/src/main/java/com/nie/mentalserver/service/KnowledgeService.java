package com.nie.mentalserver.service;

import com.nie.mental.common.result.PageResult;
import com.nie.mentalpojo.dto.ArticleCreateDTO;
import com.nie.mentalpojo.dto.ArticlePageQueryDTO;
import com.nie.mentalpojo.dto.ArticleUpdateDTO;import com.nie.mentalpojo.vo.KnowledgeArticleDetailVO;
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

    /**
     * 新增文章
     * @return 文章 ID
     */
    String createArticle(ArticleCreateDTO dto);

    /**
     * 文章详情；非管理员只能查看已发布文章，且会累加阅读量
     */
    KnowledgeArticleDetailVO articleDetail(String id, Integer userType);

    /**
     * 更新文章基础信息（分类、标题、摘要、正文、封面、标签）
     */
    void updateArticle(String id, ArticleUpdateDTO dto);

    /**
     * 发布（status=1）或下线（status=2）文章
     */
    void updateArticleStatus(String id, Integer status);

    /**
     * 删除文章（用户收藏由数据库级联删除）
     */
    void deleteArticle(String id);
}
