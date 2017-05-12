package com.wolfbe.seacos.core;


import com.wolfbe.seacos.bean.*;
import com.wolfbe.seacos.listener.EventType;
import com.wolfbe.seacos.listener.SeaListener;
import com.wolfbe.seacos.util.BlankUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Seaing是类似Spring的框架，提供了IOC容器、AOP功能，
 * 能够实现自动实例对象，为实例注入变量；
 * 使用AOP注解功能
 *
 * @author laochunyu
 * @date 2016/6/24
 * @version 1.0
 */
public class Sea {
    private static final Logger logger = LoggerFactory.getLogger(Sea.class);
    // 用于加载解析配置文件
    private SeaLoadContext loadContext;
    // 装载所有从配置文件中解析得到的bean
    private SeaBean seaBean;
    // 已初始化的实体Bean的标识列表
    private Map<String,Object> initeds = new HashMap<String,Object>();
    // 存储容器启动前监听器对象
    private List<SeaListener> blisteners;
    // 存储容器启动后监听器对象
    private List<SeaListener> flisteners;

    public Sea(String configPath) throws Exception {
        loadContext = new SeaLoadContext(configPath);
        seaBean = loadContext.getSeaBean();
    }

    /**
     * 初始化IOC容器：
     *  实例化Bean，并注入依赖
     */
    public void init() throws Exception {
        beforeListener();
        initClassBean();
        injectClassBean();
        finishListener();
    }

    /**
     * 初始化实体Bean，把SeaBean里面保存的ClassBean信息全部实例化，保存到BeanFactory里面
     */
    protected void initClassBean() throws Exception {
        Map<String,ClassBean> beanMap = seaBean.getClassBeans();
        for(String beanId: beanMap.keySet()){
            ClassBean classBean = beanMap.get(beanId);
            createClassBean(beanId,classBean);
        }
    }

    /**
     *  为实体Bean注入依赖的属性
     */
    protected void injectClassBean(){
        for(Map.Entry<String,Object> entry: initeds.entrySet()) {
            System.out.println("injectClassBean key :"+entry.getKey());
            logger.info("injectClassBean key :"+entry.getKey());
            BeanFactory.doInject(entry.getValue(),entry.getValue().getClass());
        }
    }

    /**
     * 触发执行容器启动监听器
     */
    protected void beforeListener(){
        if(blisteners!=null){
            for(SeaListener listener: blisteners){
                listener.onEvent();
            }
        }
    }

    /**
     * 触发执行容器启动完成监听器
     */
    protected void finishListener(){
        if(flisteners!=null){
            for(SeaListener listener: flisteners){
                listener.onEvent();
            }
        }
    }

    /**
     * 增加容器初始化前的监听器
     * @param listener
     */
    public void addBeforeListener(SeaListener listener){
        addSeaListener(listener, EventType.before);
    }

    /**
     * 增加容器初始化完成的监听器
     * @param listener
     */
    public void addFinishListener(SeaListener listener){
        addSeaListener(listener,EventType.finish);
    }

    protected void addSeaListener(SeaListener seaListener,EventType type){
        if(type==null)type = EventType.before;
        switch (type){
            case before:
                if(blisteners==null){
                    blisteners = new ArrayList<SeaListener>();
                }
                blisteners.add(seaListener);
                break;
            case finish:
                if(flisteners==null){
                    flisteners = new ArrayList<SeaListener>();
                }
                flisteners.add(seaListener);
                break;
        }
    }
    /**
     *  创建类实例，执行配置的初始化方法
     * @param beanId
     * @param classBean
     */
    protected void createClassBean(String beanId,ClassBean classBean) throws Exception {
        if(initeds.containsKey(beanId)){
            return;
        }
        String className = classBean.getClazz();
        Class<?> clazz = Class.forName(className);
        Object object = null;

        // 实例化ClassBean对应的对象
        ConstructorBean constructorBean = classBean.getConstructorBean();
        if(constructorBean!=null){
            LinkedHashSet<ParamBean> params = constructorBean.getParams();
            int paramSize = params.size();
            if(paramSize>0){
                Class<?> paramTypes[] = new Class[paramSize];
                Object[] paramValues = new Object[paramSize];
                // 解析方法参数，构造函数方法名等于类名
                parseParamBean(paramTypes,paramValues,params,beanId,className);
                Constructor<?> constructor = clazz.getConstructor(paramTypes);
                object = constructor.newInstance(paramValues);
            }else{  //无参构造函数
                object = clazz.newInstance();
            }
        }else{  //默认构造函数
            object = clazz.newInstance();
        }
        initeds.put(beanId, object);

        // 执行初始化方法
        Set<MethodBean> methodBeans = classBean.getMethodBeans();
        String methodName = null;
        int paramSize = 0;
        for(MethodBean methodBean: methodBeans){
            Set<ParamBean> params = methodBean.getParams();
            paramSize = params.size();
            methodName = methodBean.getName();
            if(paramSize>0){
                Class<?> paramTypes[] = new Class[paramSize];
                Object[] paramValues = new Object[paramSize];
                // 解析方法参数
                parseParamBean(paramTypes,paramValues,params,beanId,methodName);
                Method method = clazz.getMethod(methodBean.getName(),paramTypes);
                method.invoke(object,paramValues);
            }else{ // 无参方法
                Method m = clazz.getMethod(methodName);
                m.invoke(object);
            }
        }

        //保存ClassBean到BeanFactory
        String beanKey = BeanFactory.getBeanKey(beanId,className);
        BeanFactory.saveBean(beanKey,object);
        logger.info("createClassBean success,beanId is "+beanId+", className is "+className);
    }

    /**
     * 解析方法的参数ParamBean，获取参数的类型和对应的值
     * @param paramTypes
     * @param paramValues
     * @param paramBeans
     * @param beanId
     * @param methodName
     * @throws Exception
     */
    private void parseParamBean(Class<?> paramTypes[],Object[] paramValues,Set<ParamBean> paramBeans,
                                String beanId,String methodName) throws Exception {
        int i = 0;
        String ref = null;
        String type = null;
        String value = null;
        for(ParamBean paramBean: paramBeans){
            ref = paramBean.getRef();
            type = paramBean.getType();
            value = paramBean.getValue();

            if(type==null){
                paramTypes[i] = String.class;
                paramValues[i] = value;
                if(value.startsWith("{") && value.endsWith("}")){
                    String temp_id = ref.substring(1,ref.length() - 1);
                    PathBean bean = seaBean.getPathBeans().get(temp_id);
                    paramValues[i] = bean.getValue();
                }
            }else {
                switch (type) {
                    case "int":
                        paramTypes[i] = int.class;
                        paramValues[i] = Integer.parseInt(value);
                        break;
                    case "long":
                        paramTypes[i] = long.class;
                        paramValues[i] = Long.parseLong(value);
                        break;
                    case "float":
                        paramTypes[i] = float.class;
                        paramValues[i] = Float.parseFloat(value);
                        break;
                    case "double":
                        paramTypes[i] = double.class;
                        paramValues[i] = Double.parseDouble(value);
                        break;
                    case "short":
                        paramTypes[i] = short.class;
                        paramValues[i] = Short.parseShort(value);
                        break;
                    case "boolean":
                        paramTypes[i] = boolean.class;
                        paramValues[i] = Boolean.parseBoolean(value);
                        break;
                    default://枚举或者引用
                        Class<?> tmp = Class.forName(type);
                        if(tmp.isEnum()){ //枚举
                            Object[] enums = tmp.getEnumConstants();
                            for(Object obj: enums){
                                if(value.equals(obj.toString())){
                                    paramValues[i] = obj;
                                }
                            }
                        }else if(!BlankUtil.isBlank(ref)){
                            ClassBean cb = seaBean.getClassBeans().get(ref);
                            if(cb!=null){
                                createClassBean(ref,cb);
                                paramValues[i] = BeanFactory.getClassBean(BeanFactory.getBeanKey(cb.getClazz(), ref));
                            }else{
                                throw new Exception("parseParamBean param ref {"+ ref + "} is not declare,beanId:"+
                                        beanId+" methodName:"+methodName);
                            }
                        }else{
                            throw new NullPointerException("parseParamBean loadBean method ref is null! beanId is "+
                                    beanId+", methodName is "+methodName);
                        }
                        paramTypes[i] = tmp;
                        break;
                }
            }
            i++;
        }
    }
}
