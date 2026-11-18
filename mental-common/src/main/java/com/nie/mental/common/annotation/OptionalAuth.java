package com.nie.mental.common.annotation;

import java.lang.annotation.*;

/**
 * 可选登录：带 token 时解析身份，不带 token 时按匿名访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OptionalAuth {
}


