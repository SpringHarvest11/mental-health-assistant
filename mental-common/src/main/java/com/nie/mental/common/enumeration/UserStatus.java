package com.nie.mental.common.enumeration;

import lombok.Getter;

@Getter
public enum UserStatus {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常");

    private final Integer code;
    private final String description;

    UserStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
