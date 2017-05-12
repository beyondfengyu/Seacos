package com.wolfbe.seacos.core;

import com.wolfbe.seacos.anno.Exclude;
import com.wolfbe.seacos.bean.*;
import com.wolfbe.seacos.util.BlankUtil;
import com.wolfbe.seacos.util.Dom4jXmlParse;
import com.wolfbe.seacos.util.PackageUtil;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 加载Sea的配置文件，并解析得到配置的Bean信息
 * Bean信息包括：1、path bean;
 *               2、package bean;
 *               3、class bean;
 *
 * @author laochunyu
 * @date 2016/6/24
 * @version 1.0
 */
public class SeaLoadContext {
    private static final Logger logger = LoggerFactory.getLogger(SeaLoadContext.class);

    //配置文件的根元素
    private Element root;
    //配置文件
    private File configFile;
    // bean容器,装载所有从配置文件中解析得到的bean
    private SeaBean seaBean = new SeaBean();

    public SeaLoadContext(String configPath) throws Exception {
        configFile = new File(configPath);
        Dom4jXmlParse dom4j = new Dom4jXmlParse(configPath);
        root = dom4j.getRoot();
        loadAllBean();
    }

    /**
     * 解析配置文件，加载所有的Bean信息
     * 加载的顺序不能改变，避免出现依赖的问题
     */
    private void loadAllBean() throws Exception {
        parsePathBean();
        parsePackageBean();
        parseClassBean();
    }

    public SeaBean getSeaBean(){
        return seaBean;
    }
    /**
     * 解析path类型的bean，
     * 即把xml文件中path元素信息加载到SeaBean中的PathBean集合中
     * 注：需要判断元素的值代表的文件是否存在
     */
    protected void parsePathBean() throws Exception {
        Element element = (Element) root.selectSingleNode("//sea/paths");
        if(element==null){
            return;
        }
        List<?> elements = element.elements();
        for(Object obj: elements){
            Element el = (Element)obj;
            String id = el.attributeValue("id");
            String value = el.attributeValue("value");
            if(BlankUtil.isBlank(id) || BlankUtil.isBlank(value)){
                throw new NullPointerException("parsePathBean xml element id or value is null, id is "+
                        id+", value is "+value);
            }
            String path = configFile.getParent()+"/"+value;
            File file = new File(path);
            //判断文件是否存在
            if(!file.exists()){
                throw new FileNotFoundException("parsePathBean the file is not found, path is "+path+",id is "+
                        id+", value is "+value);
            }
            PathBean bean = new PathBean();
            bean.setId(id);
            bean.setValue(el.attributeValue("value"));
            seaBean.setPathBean(id, bean);
            logger.info("parsePathBean id:"+id+", value:"+bean.getValue());
        }
    }

    /**
     * 解析package类型的bean，
     * 即把xml文件中package元素信息加载到SeaBean中的PackageBean集合中，
     * 并扫描包路径下的指定类，把类信息加载到ClassBean集合
     */
    protected void parsePackageBean() throws ClassNotFoundException {
        Element element = (Element) root.selectSingleNode("//sea/ioc/packages");
        if(element==null){
            return;
        }
        List<?> elements = element.elements();
        for(Object obj: elements){
            Element el = (Element)obj;
            String text = el.getText();
            if(BlankUtil.isBlank(text)){
                throw new NullPointerException("parsePackageBean element text is null!");
            }
            PackageBean bean = new PackageBean();
            bean.setPath(text);
            seaBean.setPackageBean(bean);
            List<String> clazzes = PackageUtil.getClassesInPackage(text);
            for(String clazzStr: clazzes){
                Class<?> clazz = Class.forName(clazzStr);
                // 过滤枚举、接口、匿名类、内部类
                if(clazz.isEnum() || clazz.isInterface() || clazz.isAnonymousClass() ||clazz.isMemberClass()){
                    continue;
                }
                // 过滤标记Exclude注释的类
                Exclude exclude = (Exclude)clazz.getAnnotation(Exclude.class);
                if(exclude!=null){
                    continue;
                }
                ClassBean cbean = new ClassBean();
                cbean.setId(clazzStr);
                cbean.setClazz(clazzStr);
                seaBean.setClassBean(clazzStr, cbean);
                logger.info("parsePackageBean id:"+clazzStr+", class:"+clazzStr);
            }
        }
    }

    /**
     * 解析beans元素，
     * 即把xml文件中beans元素信息加载到SeaBean中的ClassBean集合中
     */
    protected void parseClassBean() throws Exception {
        Element element = (Element) root.selectSingleNode("//sea/ioc/beans");
        if(element==null){
            return;
        }
        //存放beans下面的class，用其判断是否有重复
        Set<String> clazzs = new HashSet<String>();
        Set<String> ids = new HashSet<String>();
        List<?> elements = element.selectNodes("bean");

        for(Object obj: elements){
            Element el = (Element)obj;
            String id = el.attributeValue("id");
            String clazz = el.attributeValue("class");

            if(BlankUtil.isBlank(id) || BlankUtil.isBlank(clazz)){
                throw new NullPointerException("parseClassBean element attribute is null, id is "+
                        id+", class is "+clazz);
            }
            if(ids.contains(id)){
                throw new Exception("parseClassBean loadBeans id repeat! id:"+id+",className:"+clazz);
            }
            if(clazzs.contains(clazz)){
                throw new Exception("parseClassBean loadBeans className repeat! id:"+id+",className:"+clazz);
            }

            ClassBean classBean = new ClassBean();
            classBean.setId(id);
            classBean.setClazz(clazz);

            // 判断是否有需要执行的方法
            List<?> methods = el.selectNodes("method");
            for (Object m : methods) {
                Element mel = (Element) m;
                String name = mel.attributeValue("name");
                if(BlankUtil.isBlank(name)){
                    throw new NullPointerException("parseClassBean loadBeans method name is null, id is "+
                            id+", class is "+clazz);
                }
                List<ParamBean> paramBeans = parseParamBean(mel);
                if(clazz.endsWith(name)){  // 构造函数
                    ConstructorBean constructorBean = new ConstructorBean();
                    for(ParamBean paramBean: paramBeans){
                        constructorBean.addParam(paramBean);
                    }
                    classBean.setConstructorBean(constructorBean);
                }else{ //普通函数
                    MethodBean methodBean = new MethodBean();
                    methodBean.setName(name);
                    for(ParamBean paramBean: paramBeans) {
                        methodBean.setParams(paramBean);
                    }
                    classBean.setMethodBeans(methodBean);
                }
            }

            // 判断是否有constructor方法需要执行
            Node conel = el.selectSingleNode("constructor");
            if(conel!=null){
                ConstructorBean constructorBean = new ConstructorBean();
                List<ParamBean> paramBeans = parseParamBean(conel);
                for(ParamBean paramBean: paramBeans){
                    constructorBean.addParam(paramBean);
                }
                classBean.setConstructorBean(constructorBean);
            }

            seaBean.setClassBean(id,classBean);
            logger.info("parseClassBean id:"+id+", class:"+classBean.getClazz());
        }
    }

    /**
     *  解析配置文件中Method的param元素，规则如下：
     *      ref    指向实例的引用beanId,
     *      value   字符串，如果用{}包含，表示引用<path>结点的值
     *      type    ref或value的类型，可以是基本类型int\long等，也可以是类的全名，如果为null，表示String类型
     * @param method
     * @throws Exception
     */
    protected List<ParamBean> parseParamBean(Node method) throws Exception {
        List<?> params = method.selectNodes("param");
        List<ParamBean> paramBeans = new ArrayList<ParamBean>();
        if(params!=null) {
            for (Object p : params) {
                Element pe = (Element) p;
                String ref = pe.attributeValue("ref");
                String value = pe.attributeValue("value");
                String type = pe.attributeValue("type");
                if(BlankUtil.isBlank(ref) && BlankUtil.isBlank(value)){
                    throw new NullPointerException("parseParamBean loadBean attribute ref and value are null!");
                }
                if(!BlankUtil.isBlank(ref) && BlankUtil.isBlank(type)){
                    throw new NullPointerException("parseParamBean loadBean attribute ref is not null,but " +
                            "type is null, ref is "+ ref);
                }
                if(BlankUtil.isBlank(value) && BlankUtil.isBlank(type)){
                    throw new NullPointerException("parseParamBean loadBean attribute value and type are  null !");
                }
                ParamBean paramBean = new ParamBean();
                paramBean.setValue(value);
                paramBean.setRef(ref);
                paramBean.setType(type);
                paramBeans.add(paramBean);
            }
        }
        return paramBeans;
    }
}
