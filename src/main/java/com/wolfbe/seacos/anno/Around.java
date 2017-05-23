package com.wolfbe.seacos.anno;


import com.wolfbe.seacos.aop.Aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识AOP切面用哪些类做切入
 * @author Andy
 * @version 1.0
 * @date 2016/7/7
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Around {

    Class<? extends Aop>[] classNames() default {};
}
