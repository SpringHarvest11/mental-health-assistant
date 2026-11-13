package com.nie.mentalpojo.vo;

import lombok.Data;

@Data
public class SystemOverviewVO {

    //总用户数
    private Long totalUsers;

    //活跃用户数
    private Long activeUsers;

    //情绪日志总数
    private Long totalDiaries;

    //今日新增日记数
    private Long todayNewDiaries;

    //咨询会话总数
    private Long totalSessions;

    //今日新增会话数
    private Long todayNewSessions;

    //平均情绪评分（0~10）
    private Double avgMoodScore;
}
