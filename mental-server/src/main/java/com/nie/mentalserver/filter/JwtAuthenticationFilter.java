package com.nie.mentalserver.filter;

import com.nie.mental.common.result.Result;
import com.nie.mental.common.result.ResultCode;
import com.nie.mental.common.utils.CurrentHolder;
import com.nie.mental.common.utils.JwtUtils;
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

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final TokenBlacklistService tokenBlacklistService;

    private final AntPathMatcher antPathMatcher =new AntPathMatcher();

    public JwtAuthenticationFilter(JwtUtils jwtUtils, ObjectMapper objectMapper, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/",
            "/api/user/login",
            "/api/user/add",
            "/v3/api-docs/**",
            "/swagger-ui/**"
    );

    /**
     * 返回 true ：跳过当前过滤器，不执行 doFilterInternal
     * @param request current HTTP request
     * @return
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String uri = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(pattern -> antPathMatcher.match(pattern,uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //获取请求头中的token
        String token = request.getHeader("token");

        //如果 token 为空，放行，让 spring security 去拦截
        if (token == null) {
            writeError(response, "令牌为空");
            return;
        }

        try {
            //解析 token
            Claims claims = jwtUtils.parseToken(token);

            //从 claims 对象中获取用户信息
            Integer userId = Integer.valueOf(claims.get("id").toString());
            String username = claims.get("username", String.class);
            Integer roleType = claims.get("userType", Integer.class);

            String jti = claims.getId();
            if (tokenBlacklistService.isBlacklisted(jti)) {
                writeError(response, "登录状态已失效，请重新登录");
                return;
            }

            //将当前登录用户id存入ThreadLocal中
            CurrentHolder.setUser(userId,roleType);


            //构建 Spring Security 的权限对象（ROLE_)
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + roleType)
            );

            //组装 Security 的认证对象
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);

            //把认证信息放入安全上下文
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            //放行
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // JwtUtils 抛出来的：过期、签名错误、格式非法
            writeError(response, e.getMessage());
            return;
        } catch (NullPointerException e) {
            writeError(response, "令牌中必要字段为空");
            return;
        } catch (ClassCastException e) {
            writeError(response, "令牌字段类型不匹配");
            return;
        } catch (Exception e) {
            // 最后兜底，防止其它未知异常逃逸
            writeError(response, "令牌解析发生未知错误");
            return;
        }finally {
            //删除ThreadLocal中的数据
            CurrentHolder.remove();
        }
    }

    /**
     * 输出统一错误 JSON
     */
    private void writeError(HttpServletResponse response, String msg) throws IOException {
        // 项目约定 HTTP 状态统一为 200，鉴权失败通过 body 中的 code=401 表达
        response.setContentType("application/json;charset=utf-8");
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), msg, null);
        objectMapper.writeValue(response.getWriter(), result);
    }
}
