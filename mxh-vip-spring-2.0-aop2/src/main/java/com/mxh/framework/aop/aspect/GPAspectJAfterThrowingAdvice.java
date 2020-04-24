package com.mxh.framework.aop.aspect;

import com.mxh.framework.aop.intercept.GPMethodInterceptor;
import com.mxh.framework.aop.intercept.GPMethodInvocation;

import java.lang.reflect.Method;

public class GPAspectJAfterThrowingAdvice extends GPAbstractAspectJAdvice implements GPMethodInterceptor {

    private String throwName;

    public GPAspectJAfterThrowingAdvice(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    @Override
    public Object invoke(GPMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        }
        catch (Throwable ex) {
          invokeAdviceMethod(mi,null,ex.getCause());
          throw  ex;
        }
    }

    public void setThrowName(String throwName) {
        this.throwName = throwName;
    }
}
