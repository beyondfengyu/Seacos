package com.wolfbe.seacos.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示不要实例化注解的类，
 * 如果类名前加上该注解，那么容器在扫描包时忽略此类
 *
 * @author Andy
 * @date 2016/6/24
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Exclude {
    String id() default "";
}
