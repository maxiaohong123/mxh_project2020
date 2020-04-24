package com.mxh.framework.aop.aspect;

import com.mxh.framework.aop.intercept.GPMethodInterceptor;
import com.mxh.framework.aop.intercept.GPMethodInvocation;

import java.lang.reflect.Method;

public class GPMethodBeforeAdviceInterceptor extends GPAbstractAspectJAdvice implements GPMethodInterceptor {

    private  GPJoinPoint jp;
    public GPMethodBeforeAdviceInterceptor(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    public  void before(Method method, Object[] arguments, Object aThis) throws Throwable{
        invokeAdviceMethod(this.jp,null,null);
    }

    @Override
    public Object invoke(GPMethodInvocation mi) throws Throwable {
        jp = mi;
       this.before(mi.getMethod(),mi.getArguments(),mi.getThis());
        return  mi.proceed();
    }
}
