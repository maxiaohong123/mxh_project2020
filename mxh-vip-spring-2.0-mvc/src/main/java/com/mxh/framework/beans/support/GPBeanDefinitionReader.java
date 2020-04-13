package com.mxh.framework.beans.support;

import com.mxh.framework.beans.config.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GPBeanDefinitionReader {

    //存储扫描到的类名，如 com.mxh.demo.action.MyAction这样的。
    private List<String> registryBeanClasses = new ArrayList<String>();
    private Properties contextConfig = new Properties();

    public GPBeanDefinitionReader(String ... configLocations){
        //1、spring支持多个配置文件，这里只支持一个
        doLoadConfig(configLocations[0]);
        //2、扫描配置路径下相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    public Properties getConfig(){
        return  this.contextConfig;
    }
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for(File file:classPath.listFiles()){
            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else{
                if(!file.getName().endsWith(".class")){continue;}
                String className = (scanPackage+"."+file.getName().replaceAll(".class",""));
                registryBeanClasses.add(className);
            }

        }
    }

    private void doLoadConfig(String configLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null!=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //将扫描到的类名变为BeanDefinitions，加载到内存中
    public List<GPBeanDefinition> loadBeanDefinitions(){
        List<GPBeanDefinition> result = new ArrayList<GPBeanDefinition>();
        try{
            for(String className:registryBeanClasses){
                Class<?> beanClass = Class.forName(className);
                //注意：此处需要判断如果该类是接口，则不需要加入beanDefinition集合，因为它的实现类会自动将接口注入的。【此处踩过坑】
                if(beanClass.isInterface()){continue;}
                //1、默认是类名首字母小写
                //clazz.getSimpleName :只获得类名，如：MyAction
                //clazz.getName: 获得全类名，如：com.mxh.demo.action.MyAction
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()),beanClass.getName()));
                //2、自定义注入名称
                //忽略
                //3、接口注入
                for(Class<?> i:beanClass.getInterfaces()){
                    result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return  result;

    }

    private GPBeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        GPBeanDefinition beanDefinition = new GPBeanDefinition();
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;

    }

    //自己写，自己用
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
//        if(chars[0] > )
        chars[0] += 32;
        return String.valueOf(chars);
    }


}
