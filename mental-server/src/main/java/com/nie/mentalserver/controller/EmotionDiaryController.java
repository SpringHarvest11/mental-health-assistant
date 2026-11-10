package com.nie.mentalserver.controller;

import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.PageResult;
import com.nie.mental.common.result.Result;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mentalpojo.dto.DiarizeDTO;
import com.nie.mentalpojo.vo.PageEmotionDiaryVO;
import com.nie.mentalserver.service.EmotionDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emotion-diary")
@Tag(name = "情绪日记")
public class EmotionDiaryController {

    @Autowired
    private EmotionDiaryService emotionDiaryService;

    /**
     * 提交情绪日记
     * @param dto
     * @return
     */
    @PostMapping
    @Operation(summary = "提交情绪日记")
    public Result<Object> diarize(@Valid @RequestBody DiarizeDTO dto){

        Integer userId = CurrentHolder.getUserId();
        if(userId == null){
            throw new BusinessException("请先登录");
        }
        emotionDiaryService.diarize(userId,dto);
        return Result.success();
    }

    /**
     * 管理员后台情绪日记分页查询
     * @param current
     * @param size
     * @param userId
     * @param moodScreRange 评分区间，如 "1-3"（字段名沿用前端拼写）
     * @return
     */
    @GetMapping("/admin/page")
    @Operation(summary = "后台情绪日志分页")
    public Result<PageResult<PageEmotionDiaryVO>> pageDiary(@RequestParam(value = "current", required = false) Integer current,
                                                            @RequestParam(value = "size", required = false) Integer size,
                                                            @RequestParam(value = "userId", required = false) Integer userId,
                                                            @RequestParam(value = "moodScreRange", required = false) String moodScreRange){//score错别字
        Integer adminId = CurrentHolder.getUserId();
        if(adminId == null){
            throw new BusinessException("请先登录");
        }
        Integer userType = CurrentHolder.getUserType();
        if(userType == null || userType == 1){
            throw new BusinessException("权限不够，无法进行该操作");
        }
        return Result.success(emotionDiaryService.pageDiary(current, size, userId, moodScreRange));
    }

    /**
     * 删除情绪日记
     * @param id
     * @return
     */
    @DeleteMapping("/admin/{id}")
    @Operation(summary = "删除情绪日记")
    public Result<Object> deleteDiary(@PathVariable("id") Integer id){
        Integer userId = CurrentHolder.getUserId();
        if(userId == null){
            throw new BusinessException("请先登录");
        }
        Integer userType = CurrentHolder.getUserType();
        if(userType == 1){
            throw new BusinessException("没有权限");
        }
        emotionDiaryService.deleteDiaryById(id);
        return Result.success();
    }


}
