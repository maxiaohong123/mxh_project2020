package com.mxh.framework.core;

public interface GPBeanFactory {
    Object getBean(String beanName) throws Exception;
    public Object getBean(Class<?> beanClass) throws Exception;
}
