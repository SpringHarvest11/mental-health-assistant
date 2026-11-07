package com.nie.mentalserver.config;

import com.nie.mental.common.utils.JwtUtils;
import com.nie.mentalserver.filter.JwtAuthenticationFilter;
import com.nie.mentalserver.service.TokenBlacklistService;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private  static final String[] PUBLIC_PATHS = {
            "/",
            "/api/user/login",
            "/api/user/add",
            "/v3/api-docs",
            "/swagger-ui/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtUtils jwtUtils,
                                                   ObjectMapper objectMapper,
                                                   TokenBlacklistService tokenBlacklistService) throws Exception {
        http
                // 禁用CSRF保护 （API服务通常不需要）
                .csrf(AbstractHttpConfigurer::disable)
                // 配置会话管理为无状态（JWT需要）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求的授权规则
                .authorizeHttpRequests(auth -> auth
                        // SSE 流式响应结束后会触发 ASYNC/ERROR 派发，需放行，否则会被判定为未认证
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        // 公开的路径，无需登录即可访问
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // 其他请求都需要认证
                        .anyRequest().authenticated()
                )
        // 添加JWT认证过滤器
        .addFilterBefore(new JwtAuthenticationFilter(jwtUtils, objectMapper, tokenBlacklistService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

