package com.nie.mentalpojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ConsultationStatsVO {

    //总会话数
    private Long totalSessions;

    //平均时长（分钟）
    private Double avgDurationMinutes;

    //每日趋势
    private List<ConsultationTrendVO> dailyTrend;
}
