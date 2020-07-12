package com.mxh.framework.context;


import com.mxh.framework.annotation.GPAutowired;
import com.mxh.framework.annotation.GPController;
import com.mxh.framework.annotation.GPService;
import com.mxh.framework.beans.GPBeanWrapper;
import com.mxh.framework.beans.config.GPBeanDefinition;
import com.mxh.framework.beans.support.GPBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 职责：完成成Bean的创建和DI
 */
public class GPApplicationContext  {

    //职责：读取配置文件，将配置文件的类变为BeanDefinition
    private GPBeanDefinitionReader beanDefinitionReader;

    //职责：存储BeanDefinition
    private Map<String,GPBeanDefinition> beanDefinitionMap = new HashMap<String,GPBeanDefinition>();

    //职责：缓存实例化后的Bean
    private Map<String,Object> factoryBeanObjectCache = new HashMap<String, Object>();

    //职责：缓存将实例化的Bean包装成的BeanWrapper。它就是传说中的IOC容器
    private Map<String,GPBeanWrapper> factoryBeanInstanceCache = new HashMap<String, GPBeanWrapper>();

    public GPApplicationContext(String ... configLocations){

        //1、加载配置文件
        beanDefinitionReader = new GPBeanDefinitionReader(configLocations);
        try{
            //2、解析配置文件，封装成BeanDefinition
            List<GPBeanDefinition> beanDefinitions = beanDefinitionReader.loadBeanDefinitions();

            //3、把BeanDefinition缓存起来
            doRegistBeanDefinition(beanDefinitions);

            //4、依赖注入
            doAutowired();
        }catch (Exception e){
            e.printStackTrace();
        }

        




    }

    private void doAutowired() {
        //依赖注入，调用getBean方法；
        //依赖注入发生的地方：显式的调用getBean方法、设置懒加载为false时。
        //到这一步，还没有真正实例化，还是在配置阶段
        for(Map.Entry<String,GPBeanDefinition> beanDefinitionEntry:this.beanDefinitionMap.entrySet())
        {
            String beanName = beanDefinitionEntry.getKey();
            getBean(beanName);
        }
    }

    //根据beanName获得一个实例
    public Object getBean(String beanName) {

        //1、先到得BeanDefinition信息
       GPBeanDefinition beanDefinition =  beanDefinitionMap.get(beanName);
        //2、将BeanDefinition实例化
       Object instance = instantiateBean(beanName,beanDefinition);
       //3、将实例化后的bean封装成一个BeanWrapper
        GPBeanWrapper beanWrapper = new GPBeanWrapper(instance);
        //4、保存到IOC容器
        factoryBeanInstanceCache.put(beanName,beanWrapper);
        //5、执行依赖注入
        populateBean(beanName,beanDefinition,beanWrapper);
       return instance;
    }

    private void populateBean(String beanName, GPBeanDefinition beanDefinition, GPBeanWrapper beanWrapper) {
//       思考：此处可能会涉及到循环依赖。
//       什么是循环依赖？如： A --> B   B-->A
//       方案：用两个缓存 ，循环两次
//       1、把第一次读取结果为空的BeanDefinition存到第一个缓存
//       2、第一次循环结束后，进行第二次循环，第二次循环时，检查为空的BeanDefinition，再进行赋值。

      Object instance =  beanWrapper.getWrappedInstance();
      //1、思考：只有该类上有@Controller和@Service注解时，才进行依赖注入；否则不进行注入。
      Class clazz =  beanWrapper.getWrappedClass();
      if(!(clazz.isAnnotationPresent(GPController.class)||clazz.isAnnotationPresent(GPService.class))){
         return;
      }

      //2、遍历该类的所有字段(public、private、protected、default类型的)
      for(Field field:clazz.getDeclaredFields()){
          if(!field.isAnnotationPresent(GPAutowired.class)){
              continue;
          }
          //3、获取到字段的Autowired注解；如果注解没有自定义名称，则用类名注入
          GPAutowired autowired = field.getAnnotation(GPAutowired.class);
          String autowiredName = autowired.value();
          if("".equals(autowiredName)){
              autowiredName = field.getType().getName();
          }
          //暴力访问(因为如果是private类型的，不设置访问，会报错的)
          field.setAccessible(true);
          if(this.factoryBeanInstanceCache.get(autowiredName)==null){
              continue;
          }

          try {
              //4、给当前类设置实例，实例对象从IOC容器中获取
              field.set(instance,this.factoryBeanInstanceCache.get(autowiredName).getWrappedInstance());
          } catch (IllegalAccessException e) {
              e.printStackTrace();
              continue;
          }

      }




    }

    //根据Class获得一个实体类
    public Object getBean(Class beanClass){
        return getBean(beanClass.getName());
    }


    //初始化Bean,创建真正的实例对象
    private Object instantiateBean(String beanName,GPBeanDefinition beanDefinition) {
        Object instance = null;
        String beanClassName = beanDefinition.getBeanClassName();
        try {

            Class<?> clazz = Class.forName(beanClassName);
            instance = clazz.newInstance();
            factoryBeanObjectCache.put(beanName,instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  instance;

    }

    private void doRegistBeanDefinition(List<GPBeanDefinition> beanDefinitions) throws Exception {
       for(GPBeanDefinition beanDefinition:beanDefinitions){
           if(this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
               throw new Exception("The "+beanDefinition.getFactoryBeanName()+" is exists");
           }
           beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
           beanDefinitionMap.put(beanDefinition.getBeanClassName(),beanDefinition);
       }

    }

}
