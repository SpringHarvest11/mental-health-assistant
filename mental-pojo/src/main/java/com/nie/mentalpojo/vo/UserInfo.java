package com.nie.mentalpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {
    private Integer id; //用户id

    private String username; //用户名

    private String email; //用户邮箱

    private String phone; //用户手机号

    private String nickname; //用户昵称

    private String avatar; //用户头像URL

    private Integer gender; //用户性别 0：未知，1：男，2：女

    private String genderDisplayName;

    private LocalDate birthday; //用户生日

    private Integer userType; //用户类型 1：普通用户；2：管理员

    private String userTypeDisplayName;

    private Integer status; //用户状态 0：禁用；1：正常

    private String statusDisplayName;

    private String displayName;

    private LocalDateTime createdAt; //创建时间

    private LocalDateTime updatedAt; //最后修改时间

}
