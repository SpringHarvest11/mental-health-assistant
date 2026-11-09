package com.nie.mentalserver.service.impl;

import com.nie.mental.common.enumeration.UserGender;
import com.nie.mental.common.enumeration.UserStatus;
import com.nie.mental.common.enumeration.UserType;
import com.nie.mental.common.exception.BusinessException;
import com.nie.mental.common.utils.JwtUtils;
import com.nie.mentalpojo.dto.UserEnrollDTO;
import com.nie.mentalpojo.dto.UserLoginDTO;
import com.nie.mentalpojo.entity.User;
import com.nie.mentalpojo.vo.UserInfoVO;
import com.nie.mentalpojo.vo.UserLoginVO;
import com.nie.mentalserver.mapper.UserMapper;
import com.nie.mentalserver.service.TokenBlacklistService;
import com.nie.mentalserver.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录实现
     * @param userLoginDTO
     * @return
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO){

        //根据用户名进行查询
        User user = userMapper.getUserByUsername(userLoginDTO.getUsername());
        if( user == null){
            //如果没查到，则根据邮箱来查询
            user = userMapper.getUserByEmail(userLoginDTO.getUsername());
        }

        //用户不存在
        if( user == null){
            throw new BusinessException("用户不存在");
        }

        //验证密码
        String inputPassword = userLoginDTO.getPassword();
        if( !passwordEncoder.matches(inputPassword, user.getPassword())){
            throw new BusinessException("密码错误");
        }

        //用户状态异常，无法登录
        if( user.getStatus().equals(UserStatus.DISABLED.getCode())){
            //抛出用户被禁用的异常
            throw new BusinessException("用户已被禁用，请联系管理员");
        }

        //生成JWT令牌
        Map<String,Object> claims = new HashMap<>();
        claims.put("id",user.getId());
        claims.put("username",user.getUsername());
        claims.put("userType",user.getUserType());
        String token = jwtUtils.generateToken(claims);

        //构建UserInfo对象
        UserInfoVO userInfo = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfo);
        if ( user.getGender().equals( UserGender.FEMALE.getGender())){
            userInfo.setGenderDisplayName(UserGender.FEMALE.getGenderDisplayName());
        }else if( user.getGender().equals( UserGender.MALE.getGender())){
            userInfo.setGenderDisplayName(UserGender.MALE.getGenderDisplayName());
        }else{
            userInfo.setGenderDisplayName(UserGender.SECRET.getGenderDisplayName());
        }

        if( user.getUserType().equals(UserType.CONSUMER.getUserType())){
            userInfo.setUserTypeDisplayName(UserType.CONSUMER.getUserTypeDisplayName());
        }else {
            userInfo.setUserTypeDisplayName(UserType.MANAGER.getUserTypeDisplayName());
        }

        if( user.getNickname() != null){
            userInfo.setDisplayName(user.getNickname());
        }else {
            userInfo.setDisplayName(user.getUsername());
        }

        if (UserStatus.NORMAL.getCode().equals(user.getStatus())){
            userInfo.setStatusDisplayName(UserStatus.NORMAL.getDescription());
        }else{
            userInfo.setStatusDisplayName(UserStatus.DISABLED.getDescription());
        }


        //封装UserLoginVO对象
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setToken(token);
        userLoginVO.setRoleType(userInfo.getUserType().toString());
        userLoginVO.setUserInfo(userInfo);


        return userLoginVO;
    }

    /**
     * 普通用户注册
     * @param userEnrollDTO
     */
    public void enroll(UserEnrollDTO userEnrollDTO){

        if(!userEnrollDTO.getPassword().equals(userEnrollDTO.getConfirmPassword())){
            throw new BusinessException("输入密码不一致");
        }
        User existUser = userMapper.getUserByUsername(userEnrollDTO.getUsername());
        if( existUser != null){
            throw new BusinessException("用户名已存在");
        }
        existUser = userMapper.getUserByEmail(userEnrollDTO.getEmail());
        if(existUser != null){
            throw new BusinessException("邮箱已使用");
        }

        String password = userEnrollDTO.getPassword();
        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .username(userEnrollDTO.getUsername())
                .email(userEnrollDTO.getEmail())
                .password(encodedPassword)
                .nickname(userEnrollDTO.getNickname())
                .phone(userEnrollDTO.getPhone())
                .gender(userEnrollDTO.getGender())
                .userType(userEnrollDTO.getUserType())
                .status(UserStatus.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);

    }

    /**
     * 退出登录，将当前令牌加入黑名单
     * @param token 请求头中的 JWT 令牌
     */
    @Override
    public void logout(String token) {
        try {
            Claims claims = jwtUtils.parseToken(token);
            //计算token剩余时间
            long remainingMillis = claims.getExpiration().getTime() - System.currentTimeMillis();
            tokenBlacklistService.blacklist(claims.getId(), remainingMillis);
        } catch (JwtException e) {
            // 令牌已失效时前端会自行清理登录态，无需再拉黑
            log.warn("退出登录时解析令牌失败: {}", e.getMessage());
        }
    }
}
