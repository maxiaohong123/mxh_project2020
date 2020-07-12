package com.mxh.framework.beans;

public class GPBeanWrapper {
    private Object wrappedInstance;
    private Class wrappedClass;

    public GPBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
        this.wrappedClass = wrappedInstance.getClass();
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    public void setWrappedInstance(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Class getWrappedClass() {
        return wrappedClass;
    }

    public void setWrappedClass(Class wrappedClass) {
        this.wrappedClass = wrappedClass;
    }
}
