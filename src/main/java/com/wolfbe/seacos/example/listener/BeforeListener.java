package com.wolfbe.seacos.example.listener;

import com.wolfbe.seacos.listener.SeaListener;

/**
 * @author laochunyu
 * @version 1.0
 * @date 2016/7/12
 */
public class BeforeListener implements SeaListener {

    @Override
    public void onEvent() {
        System.out.println("Sea init start ====================================");
//        BeanFactory.showBeanMap();
    }
}
