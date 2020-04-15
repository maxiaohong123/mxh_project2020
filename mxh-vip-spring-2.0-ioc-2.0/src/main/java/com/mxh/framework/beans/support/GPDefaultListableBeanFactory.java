package com.mxh.framework.beans.support;

import com.mxh.framework.beans.config.GPBeanDefinition;
import com.mxh.framework.context.support.GPAbstractApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class GPDefaultListableBeanFactory extends GPAbstractApplicationContext {

    //职责：存储BeanDefinition
    protected final Map<String, GPBeanDefinition> beanDefinitionMap = new HashMap<String,GPBeanDefinition>();
}
