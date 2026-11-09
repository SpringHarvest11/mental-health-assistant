package com.nie.mentalserver.service.impl;

import com.nie.mental.common.exception.BusinessException;
import com.nie.mentalpojo.dto.DiarizeDTO;
import com.nie.mentalpojo.entity.EmotionDiary;
import com.nie.mentalserver.mapper.EmotionDiaryMapper;
import com.nie.mentalserver.service.EmotionDiaryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmotionDiaryServiceImpl implements EmotionDiaryService {

    @Autowired
    private EmotionDiaryMapper emotionDiaryMapper;

    /**
     * 提交情绪日记
     * @param userId
     * @param dto
     */
    @Override
    public void diarize(Integer userId, DiarizeDTO dto){
        EmotionDiary diary = new EmotionDiary();
        BeanUtils.copyProperties(dto,diary);
        diary.setUserId(userId);
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());
        emotionDiaryMapper.insert(diary);
    }

    /**
     * 管理员根据情绪日记删除日记
     * @param id
     */
    @Override
    public void deleteDiaryById(Integer id){
        if(id == null){
            throw new BusinessException("情绪日记不存在");
        }
        emotionDiaryMapper.deleteDiaryById(id);
    }

}
