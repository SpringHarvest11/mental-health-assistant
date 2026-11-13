package com.nie.mentalpojo.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserActivityVO {

    private LocalDate date;

    private Long activeUsers;

    private Long newUsers;

    private Long diaryUsers;

    private Long consultationUsers;
}
