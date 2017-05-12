package com.wolfbe.seacos.bean;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author laochunyu
 * @version 1.0
 * @date 2016/6/27
 */
public class ClassBean {
    // 存放ID，全局必须唯一
    private String id;
    // 类的全路径
    private String clazz;
    // 构造函数Bean
    private ConstructorBean constructorBean;
    // 类初始化时需要调用的方法
    private Set<MethodBean> methodBeans = new LinkedHashSet<MethodBean>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public ConstructorBean getConstructorBean() {
        return constructorBean;
    }

    public void setConstructorBean(ConstructorBean constructorBean) {
        this.constructorBean = constructorBean;
    }

    public Set<MethodBean> getMethodBeans() {
        return methodBeans;
    }

    public void setMethodBeans(MethodBean methodBean) {
        this.methodBeans.add(methodBean);
    }
}
