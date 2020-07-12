package com.mxh.framework.beans.config;

public class GPBeanPostProcessor {

    //为Bean初始化之前提供回调接口
    public  Object postProcessBeforeInitialization(Object bean,String beanName) throws Exception{
        return  bean;
    }

    //为Bean初始化之后提供回调接口
    public  Object postProcessAfterInitialization(Object bean,String beanName) throws Exception{
        return  bean;
    }

}
