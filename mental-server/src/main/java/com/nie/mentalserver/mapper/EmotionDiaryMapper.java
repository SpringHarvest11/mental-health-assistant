package com.nie.mentalserver.mapper;

import com.nie.mentalpojo.entity.AiAnalysisTask;
import com.nie.mentalpojo.entity.EmotionDiary;
import com.nie.mentalpojo.vo.PageEmotionDiaryVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

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
    @Options(useGeneratedKeys = true, keyProperty ="id", keyColumn ="id")
    void insert(EmotionDiary diary);

    /**
     * 根据时间和用户id获取情绪日记
     * @param userId
     * @param date
     * @return
     */
    @Select("select * from emotion_diary where user_id = #{userId} and diary_date = #{date}")
    EmotionDiary getDiaryByUserIdAndDate(Integer userId, LocalDate date);

    /**
     * 覆盖当天日记内容
     * @param diary
     */
    @Update("update emotion_diary set mood_score = #{moodScore}, dominant_emotion = #{dominantEmotion}, emotion_triggers = #{emotionTriggers}," +
            "diary_content = #{diaryContent}, sleep_quality = #{sleepQuality}, stress_level =#{stressLevel}, " +
            "ai_emotion_analysis = #{aiEmotionAnalysis},ai_analysis_updated_at = #{aiAnalysisUpdatedAt}," +
            "created_at = #{createdAt},updated_at = #{updatedAt}  " +
            "where user_id = #{userId} and diary_date = #{diaryDate}")
    void updateDiary(EmotionDiary diary);

    /**
     * 管理员根据情绪日记id删除情绪日记
     * @param id
     */
    @Delete("delete from emotion_diary where id = #{id}")
    void deleteDiaryById(Integer id);

    /**
     * 删除情绪日记对应的AI任务分析表
     * @param diaryId
     */
    @Delete("delete from ai_analysis_task where diary_id = #{diaryId}")
    void deleteAiAnalysisTaskByDiaryId(Integer diaryId);

    /**
     * 向Ai任务分析表中插入一条数据
     * @param task
     */
    @Insert("insert into ai_analysis_task (diary_id, user_id, status, task_type, priority, retry_count, max_retry_count, error_message, started_at," +
            " completed_at, created_at,updated_at)" +
            " values (#{diaryId},#{userId},#{status},#{taskType},#{priority},#{retryCount},#{maxRetryCount},#{errorMessage},#{startedAt}," +
            "#{completedAt},#{createdAt},#{updatedAt})")
    void insertAiAnalysisTask(AiAnalysisTask task);

    /**
     * 管理员后台情绪日记分页查询
     * @param userId
     * @param minScore
     * @param maxScore
     * @return
     */
    List<PageEmotionDiaryVO> listDiary(Integer userId, Integer minScore, Integer maxScore);


}
