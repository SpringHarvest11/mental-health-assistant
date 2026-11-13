package com.nie.mentalpojo.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyCountVO {

    private LocalDate date;

    private Long total;
}
