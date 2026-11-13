package com.nie.mentalserver.service.impl;

import com.nie.mentalpojo.vo.*;
import com.nie.mentalserver.mapper.DataAnalyticsMapper;
import com.nie.mentalserver.service.DataAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DataAnalyticsServiceImpl implements DataAnalyticsService {

    private static final int TREND_DAYS = 7;
    private static final int ACTIVE_DAYS = 30;

    @Autowired
    private DataAnalyticsMapper dataAnalyticsMapper;

    @Override
    public AnalyticsOverviewVO overview() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(TREND_DAYS - 1L);
        LocalDate activeSince = today.minusDays(ACTIVE_DAYS - 1L);

        AnalyticsOverviewVO result = new AnalyticsOverviewVO();
        result.setSystemOverview(dataAnalyticsMapper.getSystemOverview(today, activeSince));
        result.setEmotionTrend(buildEmotionTrend(startDate, today));
        result.setConsultationStats(buildConsultationStats(startDate, today));
        result.setUserActivity(buildUserActivity(startDate, today));
        return result;
    }

    private List<EmotionTrendVO> buildEmotionTrend(LocalDate startDate, LocalDate endDate) {
        //先查数据库拿到有数据的趋势列表，转成Map：key=日期。value=VO
        Map<LocalDate, EmotionTrendVO> data = dataAnalyticsMapper
                .listEmotionTrend(startDate, endDate)
                .stream()
                .collect(Collectors.toMap(EmotionTrendVO::getDate, Function.identity()));

        List<EmotionTrendVO> result = new ArrayList<>();
        //遍历数组，dateSeries是生成连续日期序列的工具方法
        for (LocalDate date : dateSeries(startDate, endDate)) {
            EmotionTrendVO vo = data.get(date);
            if (vo == null) {
                vo = new EmotionTrendVO();
                vo.setDate(date);
                vo.setAvgMoodScore(0D);
                vo.setRecordCount(0L);
            }
            result.add(vo);
        }
        return result;
    }

    private ConsultationStatsVO buildConsultationStats(LocalDate startDate, LocalDate endDate) {
        ConsultationStatsVO stats = dataAnalyticsMapper.getConsultationSummary();
        if (stats == null) {
            stats = new ConsultationStatsVO();
            stats.setTotalSessions(0L);
            stats.setAvgDurationMinutes(0D);
        }

        Map<LocalDate, ConsultationTrendVO> data = dataAnalyticsMapper
                .listConsultationTrend(startDate, endDate)
                .stream()
                .collect(Collectors.toMap(ConsultationTrendVO::getDate, Function.identity()));

        List<ConsultationTrendVO> dailyTrend = new ArrayList<>();
        for (LocalDate date : dateSeries(startDate, endDate)) {
            ConsultationTrendVO vo = data.get(date);
            if (vo == null) {
                vo = new ConsultationTrendVO();
                vo.setDate(date);
                vo.setSessionCount(0L);
                vo.setUserCount(0L);
            }
            dailyTrend.add(vo);
        }
        stats.setDailyTrend(dailyTrend);
        return stats;
    }

    private List<UserActivityVO> buildUserActivity(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Long> activeMap = toCountMap(
                dataAnalyticsMapper.countActiveUsersByDay(startDate, endDate));
        Map<LocalDate, Long> newUserMap = toCountMap(
                dataAnalyticsMapper.countNewUsersByDay(startDate, endDate));
        Map<LocalDate, Long> diaryUserMap = toCountMap(
                dataAnalyticsMapper.countDiaryUsersByDay(startDate, endDate));
        Map<LocalDate, Long> consultationUserMap = toCountMap(
                dataAnalyticsMapper.countConsultationUsersByDay(startDate, endDate));

        List<UserActivityVO> result = new ArrayList<>();
        for (LocalDate date : dateSeries(startDate, endDate)) {
            UserActivityVO vo = new UserActivityVO();
            vo.setDate(date);
            vo.setActiveUsers(activeMap.getOrDefault(date, 0L));
            vo.setNewUsers(newUserMap.getOrDefault(date, 0L));
            vo.setDiaryUsers(diaryUserMap.getOrDefault(date, 0L));
            vo.setConsultationUsers(consultationUserMap.getOrDefault(date, 0L));
            result.add(vo);
        }
        return result;
    }

    private Map<LocalDate, Long> toCountMap(List<DailyCountVO> list) {
        return list.stream()
                .collect(Collectors.toMap(DailyCountVO::getDate, DailyCountVO::getTotal));
    }

    private List<LocalDate> dateSeries(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dates.add(date);
        }
        return dates;
    }
}
