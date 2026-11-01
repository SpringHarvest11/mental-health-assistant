package com.nie.mental.common.enumeration;

import lombok.Getter;

@Getter
public enum UserType {

    CONSUMER( 1, "普通用户"),
    MANAGER(2, "管理员");

    private final Integer userType; //用户类型 1：普通用户；2：管理员
    private final String userTypeDisplayName;

    UserType(Integer userType, String userTypeDisplayName){
        this.userType = userType;
        this.userTypeDisplayName = userTypeDisplayName;
    }
}
