package com.nie.mentalpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginVO {

    private String token;
    private UserInfo userInfo;
    private String roleType; //账号类型，1：普通用户，2：管理员
}
