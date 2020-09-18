package com.mxh.framework.aop.intercept;

public interface GPMethodInterceptor {
    Object invoke(GPMethodInvocation invocation) throws Throwable;
}
