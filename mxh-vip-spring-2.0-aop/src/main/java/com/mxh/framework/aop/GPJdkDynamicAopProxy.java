package com.mxh.framework.aop;

import com.mxh.framework.aop.support.GPAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class GPJdkDynamicAopProxy implements GPAopProxy, InvocationHandler {

    private GPAdvisedSupport advised;

    public GPJdkDynamicAopProxy(GPAdvisedSupport config) {
        this.advised = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(this.advised.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader,this.advised.getTargetClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String,GPAdvice> advices = this.advised.getInterceptorAndDynamicInterceptionAdvice(method,this.advised.getTargetClass());
        invokeAdvice(advices.get("before"));
        Object returnValue = null;
        try{
            returnValue = method.invoke(this.advised.getTarget(),args);
        }
        catch (Exception e){
         invokeAdvice(advices.get("afterThrow"));
         throw  e;
        }
           invokeAdvice(advices.get("after"));
        return  returnValue;
    }

    private  void invokeAdvice(GPAdvice advice){
        try{
            advice.getAdviceMethod().invoke(advice.getAspect());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
