package com.nie.mentalserver.filter;

import com.nie.mental.common.result.Result;
import com.nie.mental.common.result.ResultCode;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mental.common.utils.JwtUtils;
import com.nie.mentalserver.config.OptionalAuthPathRegistry;
import com.nie.mentalserver.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/",
            "/api/user/login",
            "/api/user/add",
            "/v3/api-docs/**",
            "/swagger-ui/**"
    );

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final TokenBlacklistService tokenBlacklistService;
    private final OptionalAuthPathRegistry optionalAuthPathRegistry;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtUtils jwtUtils,
                                   ObjectMapper objectMapper,
                                   TokenBlacklistService tokenBlacklistService,
                                   OptionalAuthPathRegistry optionalAuthPathRegistry) {
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
        this.tokenBlacklistService = tokenBlacklistService;
        this.optionalAuthPathRegistry = optionalAuthPathRegistry;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(pattern -> antPathMatcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 当前接口是否允许匿名
        boolean optional = optionalAuthPathRegistry.matches(request);
        String token = request.getHeader("token");

        // 没带 token
        if (token == null || token.isBlank()) {
            if (optional) {
                // 可选登录：匿名直接放行，CurrentHolder 保持为空
                filterChain.doFilter(request, response);
            } else {
                writeError(response, "令牌为空");
            }
            return;
        }

        Integer userId;
        String username;
        Integer roleType;

        // 只把「解析 token」放在 try 里，不把 filterChain.doFilter 放进来
        try {
            Claims claims = jwtUtils.parseToken(token);

            if (claims.get("id") == null || claims.get("userType") == null) {
                throw new JwtException("令牌中必要字段为空");
            }

            userId = Integer.valueOf(claims.get("id").toString());
            username = claims.get("username", String.class);
            roleType = claims.get("userType", Integer.class);

            String jti = claims.getId();
            if (tokenBlacklistService.isBlacklisted(jti)) {
                handleFailure(request, response, filterChain, "登录状态已失效，请重新登录", optional);
                return;
            }
        } catch (JwtException e) {
            // 过期、签名错误、格式非法
            handleFailure(request, response, filterChain, e.getMessage(), optional);
            return;
        } catch (NullPointerException e) {
            handleFailure(request, response, filterChain, "令牌中必要字段为空", optional);
            return;
        } catch (ClassCastException e) {
            handleFailure(request, response, filterChain, "令牌字段类型不匹配", optional);
            return;
        } catch (Exception e) {
            handleFailure(request, response, filterChain, "令牌解析发生未知错误", optional);
            return;
        }

        // 走到这里说明 token 有效，设置身份
        CurrentHolder.setUser(userId, roleType);
        try {
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + roleType)
            );
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(username, null, authorities)
            );
            filterChain.doFilter(request, response);
        } finally {
            CurrentHolder.remove();
        }
    }

    /**
     * 可选登录路径：token 无效时按匿名继续；必须登录路径：返回 401
     */
    private void handleFailure(HttpServletRequest request,
                               HttpServletResponse response,
                               FilterChain filterChain,
                               String message,
                               boolean optional) throws ServletException, IOException {
        if (optional) {
            filterChain.doFilter(request, response);
        } else {
            writeError(response, message);
        }
    }

    private void writeError(HttpServletResponse response, String msg) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), msg, null);
        objectMapper.writeValue(response.getWriter(), result);
    }
}