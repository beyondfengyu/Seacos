package com.wolfbe.seacos.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 容器根据该注解为相应的类注入依赖
 *
 * @author Andy
 * @date 2016/6/24
 * @desc
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {
    /**实例的唯一标识**/
    String id() default "";

    /**是否为单例模式，默认为true**/
    boolean isSingle() default true;

    /**实例的类对象**/
    Class<?> instance() default Object.class;
}
