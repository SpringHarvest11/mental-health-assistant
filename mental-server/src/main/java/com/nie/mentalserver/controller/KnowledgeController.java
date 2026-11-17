package com.nie.mentalserver.controller;

import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.Result;
import com.nie.mental.common.utils.CurrentHolder;
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
}
