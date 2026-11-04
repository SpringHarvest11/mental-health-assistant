package com.nie.mentalserver.service;

import com.nie.mentalpojo.dto.UserEnrollDTO;
import com.nie.mentalpojo.dto.UserLoginDTO;
import com.nie.mentalpojo.vo.UserLoginVO;

public interface UserService {

    /**
     * 用户登录失效
     * @param userLoginDTO
     * @return
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);

    /**
     * 普通用户注册
     * @param userEnrollDTO
     */
    void enroll(UserEnrollDTO userEnrollDTO);

    /**
     * 退出登录，将当前令牌加入黑名单
     * @param token 请求头中的 JWT 令牌
     */
    void logout(String token);

}
