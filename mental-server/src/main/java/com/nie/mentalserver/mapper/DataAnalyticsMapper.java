package com.nie.mentalserver.mapper;

import com.nie.mentalpojo.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DataAnalyticsMapper {


    SystemOverviewVO getSystemOverview(@Param("today") LocalDate today,
                                       @Param("activeSince") LocalDate activeSince);

    List<EmotionTrendVO> listEmotionTrend(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    ConsultationStatsVO getConsultationSummary();

    List<ConsultationTrendVO> listConsultationTrend(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    List<DailyCountVO> countNewUsersByDay(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    List<DailyCountVO> countDiaryUsersByDay(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    List<DailyCountVO> countConsultationUsersByDay(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    List<DailyCountVO> countActiveUsersByDay(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}
