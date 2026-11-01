package com.nie.mentalserver.controller;

import com.nie.mental.common.result.Result;
import com.nie.mentalpojo.dto.UserEnrollDTO;
import com.nie.mentalpojo.dto.UserLoginDTO;
import com.nie.mentalpojo.vo.UserLoginVO;
import com.nie.mentalserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Slf4j
@Tag(name="用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param userLoginDTO
     * @return Result<>
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {

        UserLoginVO userLoginVO = userService.login(userLoginDTO);
        return Result.success(userLoginVO);
    }

    /**
     * 用户注册
     * @param userEnrollDTO
     * @return Result<>
     */
    @Operation(summary = "用户注册")
    @PostMapping("/add")
    public Result enroll(@Valid @RequestBody UserEnrollDTO userEnrollDTO){
        log.info("注册参数：{}",userEnrollDTO);
        userService.enroll(userEnrollDTO);
        return Result.success();
    }

}
