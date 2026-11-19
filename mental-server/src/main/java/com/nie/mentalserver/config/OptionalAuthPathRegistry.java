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
import java.util.stream.Collectors;

@Component
public class OptionalAuthPathRegistry implements SmartInitializingSingleton {

    private final ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider;
    private final AntPathMatcher matcher = new AntPathMatcher();

    // 启动完成后一次性赋值，volatile 保证请求线程可见
    private volatile List<OptionalEndpoint> endpoints = List.of();

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
        Set<OptionalEndpoint> result = new LinkedHashSet<>();
        mapping.getHandlerMethods().forEach((info, handler) -> {
            boolean optional = handler.hasMethodAnnotation(OptionalAuth.class)
                    || handler.getBeanType().isAnnotationPresent(OptionalAuth.class);
            if (optional) {
                // 这里拿到的已经是「类上 @RequestMapping + 方法上 @GetMapping」合并后的完整路径
                Set<String> methods = info.getMethodsCondition().getMethods().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet());
                for (String pattern : info.getPatternValues()) {
                    if (methods.isEmpty()) {
                        // 未限定请求方法，所有方法都允许匿名
                        result.add(new OptionalEndpoint(null, pattern));
                    } else {
                        for (String method : methods) {
                            result.add(new OptionalEndpoint(method, pattern));
                        }
                    }
                }
            }
        });
        this.endpoints = List.copyOf(result);
    }

    public boolean matches(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        for (OptionalEndpoint endpoint : endpoints) {
            if ((endpoint.method() == null || endpoint.method().equals(method))
                    && matcher.match(endpoint.pattern(), uri)) {
                return true;
            }
        }
        return false;
    }

    private record OptionalEndpoint(String method, String pattern) {
    }
}
