package com.nie.mentalpojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class AnalyticsOverviewVO {

    private SystemOverviewVO systemOverview;

    private List<EmotionTrendVO> emotionTrend;

    private ConsultationStatsVO consultationStats;

    private List<UserActivityVO> userActivity;
}


