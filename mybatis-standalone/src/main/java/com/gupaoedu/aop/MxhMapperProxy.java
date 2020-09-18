package com.gupaoedu.aop;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MxhMapperProxy<T> implements InvocationHandler,Serializable {

    private final Class<T> mapperInterface;
    public MxhMapperProxy( Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("bbb");
        return method.invoke(mapperInterface,args);
    }
}
