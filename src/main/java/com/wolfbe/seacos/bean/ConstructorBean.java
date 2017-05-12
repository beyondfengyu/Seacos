package com.wolfbe.seacos.bean;

import java.util.LinkedHashSet;

/**
 * @author laochunyu
 * @version 1.0
 * @date 2016/6/28
 */
public class ConstructorBean {
    // 构造函数参数列表
    private LinkedHashSet<ParamBean> params = new LinkedHashSet<ParamBean>();

    public LinkedHashSet<ParamBean> getParams() {
        return params;
    }

    public void setParams(LinkedHashSet<ParamBean> params) {
        this.params = params;
    }

    public void addParam(ParamBean paramBean){
        this.params.add(paramBean);
    }
}
