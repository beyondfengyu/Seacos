package com.wolfbe.seacos.example.listener;


import com.wolfbe.seacos.core.BeanFactory;
import com.wolfbe.seacos.listener.SeaListener;

/**
 * @author Andy
 * @version 1.0
 * @date 2016/7/12
 */
public class FinishListener implements SeaListener {

    @Override
    public void onEvent() {
        System.out.println("Sea init finish ======================================");
        BeanFactory.showBeanMap();
    }
}
