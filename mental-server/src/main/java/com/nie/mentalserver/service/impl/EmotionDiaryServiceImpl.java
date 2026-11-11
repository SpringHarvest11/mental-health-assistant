package com.nie.mentalserver.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nie.mental.common.enumeration.AiAnalysisTaskStatus;
import com.nie.mental.common.enumeration.AiAnalysisTaskType;
import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.result.PageResult;
import com.nie.mentalpojo.dto.DiarizeDTO;
import com.nie.mentalpojo.entity.AiAnalysisTask;
import com.nie.mentalpojo.entity.EmotionDiary;
import com.nie.mentalpojo.vo.PageEmotionDiaryVO;
import com.nie.mentalserver.mapper.AiAnalysisTaskMapper;
import com.nie.mentalserver.mapper.EmotionDiaryMapper;
import com.nie.mentalserver.service.EmotionDiaryService;
import com.nie.mentalserver.util.EmotionAnalysisNormalizer;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmotionDiaryServiceImpl implements EmotionDiaryService {

    @Autowired
    private EmotionDiaryMapper emotionDiaryMapper;

    @Autowired
    private EmotionAnalysisNormalizer emotionAnalysisNormalizer;

    @Autowired
    private AiAnalysisTaskMapper aiAnalysisTaskMapper;

    /**
     * 提交情绪日记
     * @param userId
     * @param dto
     */
    @Override
    @Transactional
    public void diarize(Integer userId, DiarizeDTO dto){

//        //组装情绪日记对象
//        EmotionDiary diary = new EmotionDiary();
//        BeanUtils.copyProperties(dto,diary);
//        diary.setUserId(userId);
//        diary.setCreatedAt(LocalDateTime.now());
//        diary.setUpdatedAt(LocalDateTime.now());
//
//
//        //先查看该用户当天有没有记录
//        EmotionDiary old = emotionDiaryMapper.getDiaryByUserIdAndDate(userId,dto.getDiaryDate());
//
//        //组装对应的AI任务分析表
//        AiAnalysisTask task = new AiAnalysisTask();
//        task.setUserId(userId);
//        task.setStatus(AiAnalysisTaskStatus.PENDING.toString());
//        task.setTaskType(AiAnalysisTaskType.AUTO.toString());
//        task.setPriority(2);
//        task.setRetryCount(0);
//        task.setMaxRetryCount(3);
//        task.setCreatedAt(LocalDateTime.now());
//        task.setUpdatedAt(LocalDateTime.now());
//
//        //有，覆盖第一篇
//        if(old != null){
//            //覆盖上一篇情绪日记
//            emotionDiaryMapper.updateDiary(diary);
//            //删除情绪日记对应AI任务分析表
//            aiAnalysisTaskMapper.deleteAiAnalysisTaskByDiaryId(old.getId());
//            //创建新的AI任务分析表
//            task.setDiaryId(old.getId());
//            aiAnalysisTaskMapper.insert(task);
//            return;
//        }
//        //没有，新建一篇情绪日记表
//        emotionDiaryMapper.insert(diary);
//        //创建对应的AI任务分析表
//        task.setDiaryId(diary.getId());
//        aiAnalysisTaskMapper.insert(task);
        EmotionDiary diary = new EmotionDiary();
        BeanUtils.copyProperties(dto, diary);
        diary.setUserId(userId);
        diary.setAiEmotionAnalysis(null);
        diary.setAiAnalysisUpdatedAt(null);
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());

        // 有则覆盖，无则新增；依赖 unique(user_id, diary_date)
        emotionDiaryMapper.upsert(diary);

        Integer diaryId = diary.getId();
        if (diaryId == null) {
            diaryId = emotionDiaryMapper.selectIdByUserIdAndDate(userId, dto.getDiaryDate());
        }
        if (diaryId == null) {
            throw new BusinessException("保存情绪日记失败");
        }

        // 旧任务未完成就作废；已完成/失败的任务保留做历史
        emotionDiaryMapper.deleteActiveTasksByDiaryId(diaryId);

        AiAnalysisTask task = AiAnalysisTask.builder()
                .diaryId(diaryId)
                .userId(userId)
                .status(AiAnalysisTaskStatus.PENDING.toString())
                .taskType(AiAnalysisTaskType.AUTO.toString())
                .priority(2)
                .retryCount(0)
                .maxRetryCount(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        aiAnalysisTaskMapper.insert(task);
    }

    /**
     * 管理员后台情绪日记分页查询
     * @param page
     * @param size
     * @param userId
     * @param moodScoreRange 评分区间，如 "1-3"
     * @return
     */
    @Override
    public PageResult<PageEmotionDiaryVO> pageDiary(Integer page, Integer size, Integer userId, String moodScoreRange){
        if(page == null || page < 1){
            page = 1;
        }
        if(size == null || size < 1){
            size = 10;
        }
        Integer minScore = null;
        Integer maxScore = null;
        if(StringUtils.hasText(moodScoreRange)){
            String[] range = moodScoreRange.trim().split("-");
            if(range.length != 2){
                throw new BusinessException("评分区间格式不正确");
            }
            try {
                minScore = Integer.valueOf(range[0].trim());
                maxScore = Integer.valueOf(range[1].trim());
            } catch (NumberFormatException e) {
                throw new BusinessException("评分区间格式不正确");
            }
        }
        PageHelper.startPage(page,size);
        Page<PageEmotionDiaryVO> pageList = (Page<PageEmotionDiaryVO>) emotionDiaryMapper.listDiary(userId,minScore,maxScore);
        List<PageEmotionDiaryVO> list = pageList.getResult();
        // 前端会直接 JSON.parse，这里统一规范化，解析失败返回 null，避免脏数据导致详情弹窗报错
        list.forEach(vo -> vo.setAiEmotionAnalysis(
                emotionAnalysisNormalizer.format(vo.getAiEmotionAnalysis())));
        return new PageResult<>(pageList.getTotal(), list);
    }

    /**
     * 管理员根据情绪日记删除日记
     * @param id
     */
    @Override
    @Transactional
    public void deleteDiaryById(Integer id){
        if(id == null){
            throw new BusinessException("情绪日记不存在");
        }
        //删除用户相关的情绪日记
        emotionDiaryMapper.deleteDiaryById(id);
        //删除用户情绪日记对应的ai分析记录
        aiAnalysisTaskMapper.deleteAiAnalysisTaskByDiaryId(id);
    }

}
