package com.nie.mentalserver.mapper;

import com.nie.mentalpojo.entity.EmotionDiary;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmotionDiaryMapper {

    /**
     * 向情绪日记表中插入一条信息
     * @param diary
     */
    @Insert("insert into emotion_diary (user_id,diary_date,mood_score,dominant_emotion,emotion_triggers,diary_content,sleep_quality," +
            "stress_level,ai_emotion_analysis,ai_analysis_updated_at,created_at,updated_at)" +
            "values (#{userId},#{diaryDate},#{moodScore},#{dominantEmotion},#{emotionTriggers},#{diaryContent},#{sleepQuality}," +
            "#{stressLevel},#{aiEmotionAnalysis},#{aiAnalysisUpdatedAt},#{createdAt},#{updatedAt})")
    void insert(EmotionDiary diary);

    /**
     * 管理员根据情绪日记id删除情绪日记
     * @param id
     */
    @Delete("delete from emotion_diary where id = #{id}")
    void deleteDiaryById(Integer id);
}
