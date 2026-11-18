package com.nie.mentalserver.config;


import com.nie.mental.common.annotation.OptionalAuth;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class OptionalAuthPathRegistry implements SmartInitializingSingleton {

    private final ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider;
    private final AntPathMatcher matcher = new AntPathMatcher();

    // 启动完成后一次性赋值，volatile 保证请求线程可见
    private volatile List<String> patterns = List.of();

    public OptionalAuthPathRegistry(ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider) {
        this.handlerMappingProvider = handlerMappingProvider;
    }

    /**
     * 所有单例创建完成后再扫描，此时 RequestMappingHandlerMapping 已完成映射注册
     */
    @Override
    public void afterSingletonsInstantiated() {
        RequestMappingHandlerMapping mapping = handlerMappingProvider.getIfAvailable();
        if (mapping == null) {
            return;
        }
        Set<String> paths = new LinkedHashSet<>();
        mapping.getHandlerMethods().forEach((info, handler) -> {
            boolean optional = handler.hasMethodAnnotation(OptionalAuth.class)
                    || handler.getBeanType().isAnnotationPresent(OptionalAuth.class);
            if (optional) {
                // 这里拿到的已经是「类上 @RequestMapping + 方法上 @GetMapping」合并后的完整路径
                paths.addAll(info.getPatternValues());
            }
        });
        this.patterns = List.copyOf(paths);
    }

    public boolean matches(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pattern : patterns) {
            if (matcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }
}
