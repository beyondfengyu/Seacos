package com.wolfbe.seacos.example;

import com.wolfbe.seacos.core.BeanFactory;
import com.wolfbe.seacos.core.Sea;
import com.wolfbe.seacos.example.listener.BeforeListener;
import com.wolfbe.seacos.example.listener.FinishListener;
import com.wolfbe.seacos.example.service.Addr;
import com.wolfbe.seacos.example.service.ExcludeService;
import com.wolfbe.seacos.example.service.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * @author Andy
 * @version 1.0
 * @date 2016/7/12
 */
public class SeaTest {
    private final static Logger logger = LoggerFactory.getLogger(SeaTest.class);

    public static void main(String[] args) throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("sea_example.xml");
        Sea sea = null;
        if (resource != null) {
            URI uri = resource.toURI();
            File file = new File(uri);

            String path = file.getPath();
            sea = new Sea(path);
        }else{
            sea = new Sea("E:\\WebCode\\Sea\\src\\main\\resources\\sea_example.xml");
        }

        logger.info("sea is: {}",sea.toString());
        // 增加监听器
        sea.addFinishListener(new FinishListener());
        sea.addBeforeListener(new BeforeListener());
        // 容器初始化
        sea.init();

        System.out.println("---"+ BeanFactory.getBeanKey("testService1", "TestService"));
        //获取容器的Bean实例
        TestService testService1 = (TestService) BeanFactory.getClassBean(BeanFactory.getBeanKey("testService1", "TestService"));
        TestService testService2 = (TestService) BeanFactory.getClassBean(BeanFactory.getBeanKey("testService2", "TestService"));
        TestService testService3 = (TestService) BeanFactory.getClassBean(TestService.class);

//        testService1.invokeAddr("testService1");
//        testService2.invokeAddr("testService2");
        testService3.invokeAddr("testService3");
        Addr addr = (Addr) BeanFactory.getClassBean(Addr.class);
        addr.printAddr();
//        ExcludeService excludeService1 = (ExcludeService) BeanFactory.getClassBean(BeanFactory.getBeanKey("excludeService1", "ExcludeService"));
//        ExcludeService excludeService2 = (ExcludeService) BeanFactory.getClassBean(BeanFactory.getBeanKey("excludeService2", "ExcludeService"));
//        ExcludeService excludeService3 = (ExcludeService) BeanFactory.getClassBean(ExcludeService.class);
//
//        excludeService1.invokeExclude("excludeService1");
//        excludeService2.invokeExclude("excludeService2");
//        excludeService3.invokeExclude("excludeService3");

        BeanFactory.saveBean("",null);

    }
}
