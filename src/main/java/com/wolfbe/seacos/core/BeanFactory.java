package com.wolfbe.seacos.core;

import com.wolfbe.seacos.anno.Exclude;
import com.wolfbe.seacos.anno.Inject;
import com.wolfbe.seacos.util.BlankUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存储ClassBean实例
 *
 * @author laochunyu
 * @version 1.0
 * @date 2016/6/30
 */
public class BeanFactory {
    private static final Logger logger = LoggerFactory.getLogger(BeanFactory.class);
    private static Map<String, Object> beanMap = new HashMap<String, Object>();

    /**
     * beanKey一般由beanId跟className组成，这样可以避免配置中beanId相同的冲突问题，
     * 如果beanId为空，则beanKey = className
     *
     * @param beanId
     * @param className
     * @return
     */
    public static String getBeanKey(String beanId, String className) {
        if (BlankUtil.isBlank(beanId)) {
            return className;
        }
        return beanId + "_" + className;
    }

    /**
     *  判断beanKey是否存在
     * @param beanKey
     * @return
     */
    public static boolean exist(String beanKey)
    {
        return beanMap.containsKey(beanKey);
    }
    /**
     * 通过beanKey来获取ClassBean的实例
     *
     * @param beanKey
     * @return
     */
    public static Object getClassBean(String beanKey) {
        return beanMap.get(beanKey);
    }

    /**
     * 通过beanId和className来获取ClassBean的实例
     *
     * @param beanId
     * @param className
     * @return
     */
    public static Object getClassBean(String beanId, String className) {
        return beanMap.get(getBeanKey(beanId, className));
    }

    /**
     * 通过class的方式获取ClassBean的实例
     * @param clazz
     * @return
     */
    public static Object getClassBean(Class<?> clazz) {
        List<Object> list = new ArrayList<Object>();
        for(String key: beanMap.keySet()){
            Object obj = beanMap.get(key);
            // 判断clazz是否与对象obj的类对象相同，或者是obj的类对象的父类或接口
            if(clazz.isAssignableFrom(obj.getClass())){
                list.add(obj);
            }
        }
        if(list.size()>0) {
            int step = Integer.MAX_VALUE;
            Object result = null;
            for (Object obj : list) {
                int tmp = 0;
                Class objCls = obj.getClass();
                // 向父类递归，找出与clazz相同的类的层次
                while (objCls!=Object.class && clazz.isAssignableFrom(objCls)) {
                    if(clazz.getName().equals(objCls.getName())){
                        if(tmp<step){
                            result = obj;
                            step = tmp;
                        }
                        break;
                    }else{
                        objCls = objCls.getSuperclass();
                    }
                    tmp++;
                }
            }

            if(result!=null){
                return result;
            }else{
                return list.get(0);
            }
        }
        return null;
    }

    /**
     * 保存ClassBean实例到BeanFactory
     *
     * @param beanKey
     * @param classBean
     */
    public static void saveBean(String beanKey, Object classBean) {
        beanMap.put(beanKey, classBean);
    }

    /**
     * 实例化对象，并注入依赖
     * @param beanId
     * @param className
     * @throws Exception
     */
    public static void registBean(String beanId,String className) throws Exception{
        if(isExist(getBeanKey(beanId,className))){
            return;
        }
        Class<?> clazz = Class.forName(className);

        Exclude exclude = clazz.getAnnotation(Exclude.class);
        // Exclude注释、匿名类、枚举、内部类都忽略
        if(exclude!=null || clazz.isAnonymousClass() || clazz.isEnum() || clazz.isMemberClass()){
            return;
        }
        Object object = null;
        // 标识是否使用默认构造函数
        boolean isDefault = false;
        Constructor<?>[] constructions = clazz.getConstructors();
        for(Constructor<?> constructor: constructions){
            Class[] types = constructor.getParameterTypes();
            if(types==null || types.length==0){
                isDefault = true;
                break;
            }
        }
        if(isDefault){
            // 使用默认构造函数
            object = clazz.newInstance();
        }else{
            if(constructions.length>1){
                logger.error("BeanFactory registBean error when initiating " + className+",not confirm which constructor");
                return;
            }else{
                Constructor<?> constructor = constructions[0];
                Class[] paramTypes = constructor.getParameterTypes();
                Object[] paramValues = new Object[paramTypes.length];
                for(int i=0; i<paramTypes.length; i++){
                    Class<?> paramType = paramTypes[i];
                    Object paramValue = getClassBean(paramType);
                    if(paramValue==null){
                        logger.error("BeanFactory regist error when initiating " + className+",constructor init error ," +
                                "not constrnctor param object");
                        return;
                    }else{
                        paramValues[i] = paramValue;
                    }
                }
                object = constructor.newInstance(paramValues);
            }
        }

        //TODO AOP功能


        // 存储生成的实例对象
        saveBean(getBeanKey(beanId, className), object);
        // 为实例注入依赖
        doInject(object,clazz);
        logger.info("registBean success,beanId is "+beanId+", className is "+className);
    }

    /**
     * 注入依赖
     * @param object
     * @param clazz
     */
    public static void doInject(Object object,Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                Inject inject = field.getAnnotation(Inject.class);
                if (inject == null) {
                    continue;
                }
                String id = inject.id();
                String instance = inject.instance().getName();
                Class<?> type = field.getType();
                String className = type.getName();
                if (BlankUtil.isBlank(instance)) {
                    if (type.isInterface()) {
                        throw new RuntimeException("doInject the field's type is a interface, field id is " +
                                id + ", type is " + className);
                    }
                } else if(!instance.equals(Object.class.getName())){
                    className = instance;
                }
                Object vObj = null;
                if (inject.isSingle()) { // 单例
                    vObj = getClassBean(id, className);
                    if (vObj == null) {
                        registBean(id,className);
                        vObj = getClassBean(id, className);
                    }
                } else { // 非单例
                    vObj = Class.forName(className).newInstance();
                }
                field.setAccessible(true);
                field.set(object, vObj);

                //递归注入父类的依赖
                Class<?> superClass = object.getClass().getSuperclass();
                if(superClass==Object.class){
                    return;
                }else {
                    doInject(object, superClass);
                }
            }
        } catch (Exception e) {
            logger.error("doInject fail,cause by " + e.getMessage(), e);
        }
    }

    public static boolean isExist(String beanKey){
        return beanMap.containsKey(beanKey);
    }

    public static void showBeanMap(){
        System.out.println("showBeanMap ==========================================");
        for(String key: beanMap.keySet()){
            logger.info("bean key: ["+key+"],  object: "+beanMap.get(key).getClass().getName());
//            System.out.println("bean key: ["+key+"],  object: "+beanMap.get(key).getClass().getName());
        }
    }
}
