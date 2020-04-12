package com.mxh.framework.aop;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class GPAdvice {

    private Object aspect;
    private Method adviceMethod;
    private String throwName;


    public GPAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }
}
