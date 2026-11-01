package com.nie.mentalpojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UserEnrollDTO {

    //用户名
    @NotBlank(message = "用户名不能为空")
    private String username;

    //邮箱
    @NotBlank(message = "邮箱不能为空")
    private String email;

    //手机号
    private String phone;

    //昵称
    private String nickname;

    //密码
    @NotBlank(message = "密码不能为空")
    private String password;

    //密码确认
    @NotBlank(message = "请输入确认密码")
    private String confirmPassword;


    //性别 0：未知，1：男，2：女
    private Integer gender;

    //用户类型1：普通用户；2：管理员（默认为普通用户）
    private Integer userType  = 1;
}
