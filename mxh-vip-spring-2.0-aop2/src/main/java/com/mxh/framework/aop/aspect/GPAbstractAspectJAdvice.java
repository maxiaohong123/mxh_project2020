package com.mxh.framework.aop.aspect;

import java.lang.reflect.Method;

public  abstract class GPAbstractAspectJAdvice implements GPAdvice {

    private Object aspect;
    private Method adviceMethod;
    private String throwName;

    public GPAbstractAspectJAdvice(Object aspect,Method adviceMethod){
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }

    public  Object invokeAdviceMethod(GPJoinPoint jp,Object returnValue,Throwable ex) throws Throwable{
        Class<?>[] paramTypes = this.adviceMethod.getParameterTypes();
        if(null == paramTypes||paramTypes.length==0){
            return  this.adviceMethod.invoke(this.aspect);
        }else{
            Object[] args = new Object[paramTypes.length];
            for(int i=0;i<paramTypes.length;i++){
                if(paramTypes[i]==GPJoinPoint.class){
                    args[i] = jp;
                }else if(paramTypes[i] == Throwable.class){
                    args[i] = ex;
                }else if(paramTypes[i] == Object.class){
                    args[i] = returnValue;
                }

            }
            return  this.adviceMethod.invoke(aspect,args);
        }
    }



}
