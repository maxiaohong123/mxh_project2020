package com.mxh.framework.core;

public interface GPBeanFactory {

    //根据bean的名字，获取在IOC容器中得到bean实例
    Object getBean(String name) ;

    Object getBean(Class beanClass);
}
