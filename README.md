# Seacos
 仿Spring实现的轻量级IOC框架，线上大型网站验证：
 * 支持注解和配置两种注入方式；
 * 支持拦截监听；
 * 稳定可靠，简单易用；
 <br><br>
## 使用
 可以参照exmple包下例子，源码如下：
 ```
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
  ```
  <br>
  1 首先，获取Seacos的配置文件所在的类路径；
  2 然后，添加自定义的监听器，继承框架的监听器接口来实现自定义监听器；
  3 最后，调用BeanFactory获取指定名称的Bean；
  <br><br>
## 交流
如果有兴趣交流Netty相关知识，可以加入**Netty联盟：379119816**    **Java联盟：399643539**
<br><br>
