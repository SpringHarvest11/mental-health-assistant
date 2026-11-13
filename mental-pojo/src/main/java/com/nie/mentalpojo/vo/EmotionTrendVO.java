package com.nie.mentalpojo.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmotionTrendVO {

    //日期，如 2026-11-01
    private LocalDate date;

    //当日平均情绪评分
    private Double avgMoodScore;

    //当日记录数量
    private Long recordCount;
}
