package com.wolfbe.seacos.aop;

import java.lang.reflect.Method;

/**
 * @author laochunyu
 * @version 1.0
 * @date 2016/7/7
 */
public interface Aop {

    /***
     * 方法执行前，会执行的方法
     *
     * @return
     * @throws Exception
     */
    public void before(Method method, Object[] args) throws Exception;

    /**
     * 方法执行后，会执行的方法
     *
     * @return
     * @throws Exception
     */
    public void after(Method method, Object[] args) throws Exception;

}
