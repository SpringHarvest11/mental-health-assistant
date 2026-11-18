package com.nie.mentalserver.controller;

import com.nie.mental.common.annotation.OptionalAuth;
import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.PageResult;
import com.nie.mental.common.result.Result;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mentalpojo.dto.ArticlePageQueryDTO;
import com.nie.mentalpojo.vo.KnowledgeArticlePageVO;
import com.nie.mentalpojo.vo.KnowledgeCategoryTreeVO;
import com.nie.mentalserver.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@Tag(name = "知识库")
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    @Operation(summary = "文章分类树")
    @GetMapping("/category/tree")
    public Result<List<KnowledgeCategoryTreeVO>> categoryTree (){
        if(CurrentHolder.getUserId() == null){
            throw new BusinessException("暂未登录，请先登录");
        }
        return Result.success(knowledgeService.categoryTree());
    }

    @OptionalAuth  // 就是这个接口允许匿名；带 token 时仍能拿到管理员身份
    @GetMapping("/article/page")
    @Operation(summary = "文章分页")
    public Result<PageResult<KnowledgeArticlePageVO>> page (ArticlePageQueryDTO query){
        Integer userType = CurrentHolder.getUserType();
        return Result.success(knowledgeService.pageArticles(userType,query));
    }
}
