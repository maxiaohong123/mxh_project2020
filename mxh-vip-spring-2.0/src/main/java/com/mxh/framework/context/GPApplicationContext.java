package com.mxh.framework.context;


import com.mxh.framework.beans.config.GPBeanDefinition;
import com.mxh.framework.beans.support.GPBeanDefinitionReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 职责：完成成Bean的创建和DI
 */
public class GPApplicationContext  {

    private GPBeanDefinitionReader beanDefinitionReader;

    private Map<String,GPBeanDefinition> beanDefinitionMap = new HashMap<String,GPBeanDefinition>();

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

    private void getBean(String beanName) {

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
