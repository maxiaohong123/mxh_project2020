package com.mxh.framework.aop;

import com.mxh.framework.aop.support.GPAdvisedSupport;

public class GPDefaultAopProxyFactory {

    public GPAopProxy createAopProxy(GPAdvisedSupport config) throws Exception{
         Class targetClass = config.getTargetClass();
         if(targetClass.getInterfaces().length>0){
             return  new GPJdkDynamicAopProxy(config);
         }

        return  new GPCglibAopProxy();
    }
}
