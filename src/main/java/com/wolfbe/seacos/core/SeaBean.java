package com.wolfbe.seacos.core;


import com.wolfbe.seacos.bean.ClassBean;
import com.wolfbe.seacos.bean.PackageBean;
import com.wolfbe.seacos.bean.PathBean;

import java.util.*;

/**
 * 装载配置文件里面解析出来的所有Bean信息
 *
 * @author laochunyu
 * @version 1.0
 * @date 2016/6/24
 */
public class SeaBean {

    private Map<String,PathBean> pathBeans = new HashMap<String,PathBean>();
    private Set<PackageBean> packageBeans = new LinkedHashSet<PackageBean>();
    private Map<String,ClassBean> classBeans = new LinkedHashMap<String,ClassBean>();

    public void setPathBean(String id,PathBean pathBean){
        this.pathBeans.put(id, pathBean);
    }

    public void setPackageBean(PackageBean packageBean){
        this.packageBeans.add(packageBean);
    }

    public void setClassBean(String id,ClassBean classBean){
        this.classBeans.put(id, classBean);
    }

    public Map<String,PathBean> getPathBeans(){
        return this.pathBeans;
    }

    public Set<PackageBean> getPackageBeans(){
        return this.packageBeans;
    }

    public Map<String,ClassBean>  getClassBeans(){
        return this.classBeans;
    }


}
