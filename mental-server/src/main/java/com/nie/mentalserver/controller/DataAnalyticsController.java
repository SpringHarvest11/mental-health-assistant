package com.nie.mentalserver.controller;

import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.Result;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mentalpojo.vo.AnalyticsOverviewVO;
import com.nie.mentalserver.service.DataAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-analytics")
@Tag(name = "数据分析")
public class DataAnalyticsController {

    @Autowired
    private DataAnalyticsService dataAnalyticsService;

    @GetMapping("/overview")
    @Operation(summary = "后台数据总览")
    public Result<AnalyticsOverviewVO> overview() {
        Integer userId = CurrentHolder.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        Integer userType = CurrentHolder.getUserType();
        if (userType == null || userType == 1) {
            throw new BusinessException("权限不够，无法进行该操作");
        }
        return Result.success(dataAnalyticsService.overview());
    }
}
