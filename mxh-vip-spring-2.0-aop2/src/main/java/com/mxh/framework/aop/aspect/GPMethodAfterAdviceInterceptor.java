package com.mxh.framework.aop.aspect;

import com.mxh.framework.aop.intercept.GPMethodInterceptor;
import com.mxh.framework.aop.intercept.GPMethodInvocation;

import java.lang.reflect.Method;

public class GPMethodAfterAdviceInterceptor  extends GPAbstractAspectJAdvice implements  GPMethodInterceptor {
    private  GPJoinPoint jp;
    public GPMethodAfterAdviceInterceptor(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    private  void afterReturning( Object returnValue, Method method, Object[] args, Object target) throws Throwable{
        this.invokeAdviceMethod(this.jp,returnValue,null);
    }
    @Override
    public Object invoke(GPMethodInvocation mi) throws Throwable {
        jp = mi;
        Object retVal = mi.proceed();
        this.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }
}
