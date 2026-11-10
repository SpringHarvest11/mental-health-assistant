package com.nie.mentalpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageEmotionDiaryVO {

    //日记id（详情、删除使用）
    private Integer id;

    //用户id
    private Integer userId;

    //用户名
    private String username;

    //昵称
    private String nickname;

    //记录日期 YYYY-MM-DD
    private LocalDate diaryDate;

    //情绪评分 1~10
    private Integer moodScore;

    //主要情绪
    private String dominantEmotion;

    //触发因素
    private String emotionTriggers;

    //日记正文
    private String diaryContent;

    //睡眠质量 1~5
    private Integer sleepQuality;

    //压力水平 1~5
    private Integer stressLevel;

    //AI 分析结果 JSON 字符串
    private String aiEmotionAnalysis;

    //创建时间
    private LocalDateTime createdAt;

    //更新时间
    private LocalDateTime updatedAt;
}
