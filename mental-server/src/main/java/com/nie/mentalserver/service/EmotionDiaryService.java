package com.nie.mentalserver.service;

import com.nie.mentalpojo.dto.DiarizeDTO;

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
}
