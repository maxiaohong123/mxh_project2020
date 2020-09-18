package com.gupaoedu.aop;


import java.lang.reflect.Proxy;

public class MxhMapperProxyFactory<T> {

    private final Class<T> mapperInterface;

    public MxhMapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }


    protected T newInstance(MxhMapperProxy<T> mapperProxy) {
       T proxy =  (T) Proxy.newProxyInstance(this.mapperInterface.getClassLoader(),new Class[]{this.mapperInterface},mapperProxy);
       return proxy;
    }

    public T newInstance() {
        MxhMapperProxy<T> mapperProxy = new MxhMapperProxy(this.mapperInterface);
        return newInstance(mapperProxy);
    }
}
