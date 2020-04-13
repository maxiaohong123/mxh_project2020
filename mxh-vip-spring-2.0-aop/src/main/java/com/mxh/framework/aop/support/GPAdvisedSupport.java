package com.mxh.framework.aop.support;

import com.mxh.framework.aop.GPAdvice;
import com.mxh.framework.aop.config.GPAopConfig;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class GPAdvisedSupport {

    private Class<?> targetClass;

    private Object target;

    private GPAopConfig config;

    private Pattern pointCutClassPattern;

    private transient Map<Method, Map<String, GPAdvice>> methodCache;

    public GPAdvisedSupport(GPAopConfig config) {
        this.config = config;
    }

    public Class<?> getTargetClass(){
        return  this.targetClass;
    }

    public Object getTarget(){
        return  this.target;
    }

    public Map<String,GPAdvice> getInterceptorAndDynamicInterceptionAdvice(Method method,Class<?> targetClass) throws Exception{
        Map<String,GPAdvice> cached = methodCache.get(method);
        if(cached == null){
            Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());
            cached = methodCache.get(m);
        }

        return  cached;
    }

    public void setTargetClass(Class<?> targetClass){
        this.targetClass  = targetClass;
        parse();
    }

    private void parse() {
        String pointCut = config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");
        String pointCutForClassRegex = pointCut.substring(0,pointCut.lastIndexOf("\\(")-4);
        //注意：此处的"class  "后面一定要有空格，因为它匹配：class ModifyService{}这样的内容。
        pointCutClassPattern = Pattern.compile("class "+pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ")+1));

        try{
            methodCache = new HashMap<Method, Map<String, GPAdvice>>();
            Pattern pattern = Pattern.compile(pointCut);

            Class aspectClass = Class.forName(this.config.getAspectClass());
            Map<String,Method> aspectMethods = new HashMap<String,Method>();

            for(Method method:aspectClass.getMethods()){
                aspectMethods.put(method.getName(),method);
            }

            for(Method method:this.targetClass.getMethods()){
                String methodString = method.toString();
                if(methodString.contains("throws")){
                    methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
                }

                Matcher matcher = pattern.matcher(methodString);
                if(matcher.matches()){
                    Map<String,GPAdvice> advices = new HashMap<String, GPAdvice>();

                    //把每一个方法包装成MethodInceptor

                    //before

                    if(!(null == config.getAspectBefore()||"".equals(config.getAspectBefore()))){
                        advices.put("before",new GPAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectBefore())));
                    }

                    //after

                    if(!(null == config.getAspectAfter()||"".equals(config.getAspectAfter()))){
                        advices.put("after",new GPAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfter())));
                    }

                    //afterThrowing

                    if(!(null == config.getAspectAfterThrow()||"".equals(config.getAspectAfterThrow()))){
                        GPAdvice throwingAdvice = new GPAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfterThrow()));
                        throwingAdvice.setThrowName(config.getAspectAfterThrowingName());
                        advices.put("afterThrow",throwingAdvice);
                    }

                    methodCache.put(method,advices);

                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public  void setTarget(Object target){
        this.target = target;
    }

    public  boolean pointCutMatch(){
        return  pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }
}
