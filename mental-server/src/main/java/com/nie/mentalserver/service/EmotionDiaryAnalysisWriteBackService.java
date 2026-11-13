package com.nie.mentalserver.service;

import com.nie.mentalpojo.entity.EmotionDiary;

public interface EmotionDiaryAnalysisWriteBackService {

    public boolean writeBack(Integer taskId, EmotionDiary snapshot, String analysisJson);
}
