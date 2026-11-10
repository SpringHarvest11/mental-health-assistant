package com.nie.mentalserver.service;

import com.nie.mental.common.result.PageResult;
import com.nie.mentalpojo.dto.DiarizeDTO;
import com.nie.mentalpojo.vo.PageEmotionDiaryVO;

/**
 * 情绪日记模块
 */
public interface EmotionDiaryService {

    /**
     * 提交情绪日记
     * @param uderId
     * @param dto
     */
    void diarize(Integer uderId, DiarizeDTO dto);

    /**
     * 管理员根据情绪日记删除日记
     * @param id
     */
    void deleteDiaryById(Integer id);

    /**
     * 管理员后台情绪日记分页查询
     * @param page
     * @param size
     * @param userId
     * @param moodScoreRange 评分区间，如 "1-3"
     * @return
     */
    PageResult<PageEmotionDiaryVO> pageDiary(Integer page, Integer size, Integer userId, String moodScoreRange);
}
