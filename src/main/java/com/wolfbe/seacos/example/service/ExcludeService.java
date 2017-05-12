package com.wolfbe.seacos.example.service;


import com.wolfbe.seacos.anno.Exclude;

/**
 * @author laochunyu
 * @version 1.0
 * @date 2016/7/8
 */
@Exclude
public class ExcludeService {


    //
    public void init(String str){
        System.out.println("ExcludeService init() , str is "+str);
    }

    public void finish(String str){
        System.out.println("ExcludeService finish() , str is "+str);
    }

    public void invokeExclude(String str){
        System.out.println("invokeExclude success, str is "+str);
    }
}
