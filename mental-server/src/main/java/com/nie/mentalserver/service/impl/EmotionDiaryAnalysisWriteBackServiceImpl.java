package com.nie.mentalserver.service.impl;

import com.nie.mentalpojo.entity.EmotionDiary;
import com.nie.mentalserver.mapper.AiAnalysisTaskMapper;
import com.nie.mentalserver.mapper.EmotionDiaryMapper;
import com.nie.mentalserver.service.EmotionDiaryAnalysisWriteBackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class EmotionDiaryAnalysisWriteBackServiceImpl implements EmotionDiaryAnalysisWriteBackService {

    private final EmotionDiaryMapper emotionDiaryMapper;
    private final AiAnalysisTaskMapper aiAnalysisTaskMapper;

    public EmotionDiaryAnalysisWriteBackServiceImpl(EmotionDiaryMapper emotionDiaryMapper,
                                                AiAnalysisTaskMapper aiAnalysisTaskMapper) {
        this.emotionDiaryMapper = emotionDiaryMapper;
        this.aiAnalysisTaskMapper = aiAnalysisTaskMapper;
    }

    /**
     * 条件写回：任务仍 PROCESSING 且日记内容仍是快照时才写入
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean writeBack(Integer taskId, EmotionDiary snapshot, String analysisJson) {

        int updated = emotionDiaryMapper.updateAnalysisIfSnapshotMatches(
                taskId, snapshot, analysisJson, LocalDateTime.now());

        if (updated == 1) {
            aiAnalysisTaskMapper.complete(taskId);
            return true;
        }

        // 0 行有两种可能：已被覆盖/删除，或者值本来就相同
        if (emotionDiaryMapper.isSnapshotStillValid(taskId, snapshot) == 0) {
            log.info("情绪分析结果已过期，丢弃: taskId={}, diaryId={}", taskId, snapshot.getId());
            aiAnalysisTaskMapper.markOrphan(taskId, "日记已被覆盖或删除");
            return false;
        }

        // 条件仍成立，只是分析结果和库里一样
        aiAnalysisTaskMapper.complete(taskId);
        return true;
    }
}
