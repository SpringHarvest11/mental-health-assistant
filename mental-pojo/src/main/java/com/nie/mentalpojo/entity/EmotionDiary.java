package com.nie.mentalpojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmotionDiary {

    //日记id
    private Integer id;

    //用户id
    private Integer userId;

    //日记日期
    private LocalDate diaryDate;

    //情绪评分（1到10）
    private Integer moodScore;

    //主要情绪
    private String dominantEmotion;

    //情绪触发因素
    private String emotionTriggers;

    //日记内容
    private String diaryContent;

    //睡眠质量（1到5）
    private Integer sleepQuality;

    //压力水平（1到5）
    private Integer stressLevel;

    //AI情绪分析结果(JSON格式）
    private String aiEmotionAnalysis;

    //AI分析更新时间
    private LocalDateTime aiAnalysisUpdatedAt;

    //创建时间
    private LocalDateTime createdAt;

    //最后修改时间
    private LocalDateTime updatedAt;
}
