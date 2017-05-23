package com.wolfbe.seacos.example.service;


import com.wolfbe.seacos.anno.Inject;

/**
 * @author Andy
 * @version 1.0
 * @date 2016/7/8
 */
public class TestService {
    // 注入
    @Inject
    private Addr addr;

    //
    public void init(String str){
        System.out.println("TestService init() , str is "+str);
    }

    public void finish(String str){
        System.out.println("TestService finish() , str is "+str);
    }
    public void invokeAddr(String str){
        addr.printAddr();
        System.out.println("invokeAddr success, str is "+str);
    }

}
