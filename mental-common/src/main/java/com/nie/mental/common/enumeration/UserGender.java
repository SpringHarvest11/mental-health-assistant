package com.nie.mental.common.enumeration;

import lombok.Getter;

@Getter
public enum UserGender {

    SECRET(0, "未知"),
    MALE( 1,"男" ),
    FEMALE( 2, "女");

    private final Integer gender;
    private final String genderDisplayName;

    UserGender(Integer gender, String genderDisplayName){
        this.gender = gender;
        this.genderDisplayName = genderDisplayName;
    }
}
