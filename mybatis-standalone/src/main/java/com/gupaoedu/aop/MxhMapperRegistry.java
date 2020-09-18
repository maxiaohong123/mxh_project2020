package com.gupaoedu.aop;

import org.apache.ibatis.binding.BindingException;

public class MxhMapperRegistry {

    public <T> T getMapper(Class<T> type) {
        final MxhMapperProxyFactory<T> mapperProxyFactory = new MxhMapperProxyFactory<T>(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MxhMapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance();
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }
}
