package com.nie.mentalserver.controller;

import com.nie.mental.common.annotation.OptionalAuth;
import com.nie.mental.common.enumeration.UserType;
import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.PageResult;
import com.nie.mental.common.result.Result;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mentalpojo.dto.ArticleCreateDTO;
import com.nie.mentalpojo.dto.ArticlePageQueryDTO;
import com.nie.mentalpojo.dto.ArticleStatusDTO;
import com.nie.mentalpojo.dto.ArticleUpdateDTO;
import com.nie.mentalpojo.vo.KnowledgeArticleDetailVO;
import com.nie.mentalpojo.vo.KnowledgeArticlePageVO;
import com.nie.mentalpojo.vo.KnowledgeCategoryTreeVO;
import com.nie.mentalserver.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/article")
    @Operation(summary = "新增文章")
    public Result<String> create(@Valid @RequestBody ArticleCreateDTO dto){
        requireAdmin();
        return Result.success(knowledgeService.createArticle(dto));
    }

    @PutMapping("/article/{id}")
    @Operation(summary = "更新文章")
    public Result<Void> update(@PathVariable String id, @Valid @RequestBody ArticleUpdateDTO dto){
        requireAdmin();
        knowledgeService.updateArticle(id, dto);
        return Result.success();
    }

    @PutMapping("/article/{id}/status")
    @Operation(summary = "发布/下线文章")
    public Result<Void> updateStatus(@PathVariable String id, @Valid @RequestBody ArticleStatusDTO dto){
        requireAdmin();
        knowledgeService.updateArticleStatus(id, dto.getStatus());
        return Result.success();
    }

    @DeleteMapping("/article/{id}")
    @Operation(summary = "删除文章")
    public Result<Void> delete(@PathVariable String id){
        requireAdmin();
        knowledgeService.deleteArticle(id);
        return Result.success();
    }

    @OptionalAuth  // 前台匿名可看；后台编辑带 token 可看草稿/下线文章
    @GetMapping("/article/{id}")
    @Operation(summary = "文章详情")
    public Result<KnowledgeArticleDetailVO> detail(@PathVariable String id){
        return Result.success(knowledgeService.articleDetail(id, CurrentHolder.getUserType()));
    }

    private void requireAdmin() {
        if (!UserType.MANAGER.getUserType().equals(CurrentHolder.getUserType())) {
            throw new BusinessException("无权限操作文章");
        }
    }
}
