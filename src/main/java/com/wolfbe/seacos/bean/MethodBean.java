package com.wolfbe.seacos.bean;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Andy
 * @version 1.0
 * @date 2016/6/28
 */
public class MethodBean {

    // 方法名称
    private String name;
    // 方法参数列表
    private Set<ParamBean> params = new LinkedHashSet<ParamBean>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ParamBean> getParams() {
        return params;
    }

    public void setParams(ParamBean paramBean) {
        this.params.add(paramBean);
    }
}
