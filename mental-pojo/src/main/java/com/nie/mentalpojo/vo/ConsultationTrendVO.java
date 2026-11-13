package com.nie.mentalpojo.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ConsultationTrendVO {

    private LocalDate date;

    private Long sessionCount;

    private Long userCount;
}
