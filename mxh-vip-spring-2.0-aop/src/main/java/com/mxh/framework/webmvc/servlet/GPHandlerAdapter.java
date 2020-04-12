package com.mxh.framework.webmvc.servlet;

import com.mxh.framework.annotation.GPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GPHandlerAdapter {


    public GPModelAndView handle(HttpServletRequest req, HttpServletResponse resp, GPHandlerMapping handler)  throws Exception{
        // 责任：保存形参列表。将参数名称和参数的位置关系保存起来。
        Map<String,Integer> paramIndexMappings = new HashMap<String, Integer>();

        Annotation[][] pa = handler.getMethod().getParameterAnnotations();
        for(int i=0;i<pa.length;i++){
            for(Annotation a:pa[i]){
                if(a instanceof GPRequestParam){
                    String paramName = ((GPRequestParam) a).value();
                    if(!"".equals(paramName.trim())){   //注意：这里没判断空，此处踩过坑。必须判断
                        paramIndexMappings.put(paramName,i);
                    }

                }
            }
        }
        //保存HttpServletRequest和HttpServletResponse的参数位置。
        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();
        for(int i=0;i<paramTypes.length;i++){
            Class<?> paramType = paramTypes[i];
            if(paramType==HttpServletRequest.class||paramType==HttpServletResponse.class){
                paramIndexMappings.put(paramType.getName(),i);
            }
        }

       //拼接实参列表
        //http://localhost/web/query?name=Tom&Cat
        Map<String,String[]> params = req.getParameterMap();

        Object[] paramValues = new Object[paramTypes.length];

        for(Map.Entry<String,String[]> param:params.entrySet())
        {
            String value = Arrays.toString(params.get(param.getKey())).replaceAll("\\[|\\]","").replaceAll("\\s+",",");
            if(!paramIndexMappings.containsKey(param.getKey())){
                continue;
            }
            int index = paramIndexMappings.get(param.getKey());

            //允许定自义类型转换器Convert
            paramValues[index] = castStringValue(value,paramTypes[index]);
        }

        if(paramIndexMappings.containsKey(HttpServletRequest.class.getName())){
           int index = paramIndexMappings.get(HttpServletRequest.class.getName());
           paramValues[index] = req;
        }

        if(paramIndexMappings.containsKey(HttpServletResponse.class.getName())){
            int index = paramIndexMappings.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        Object result = handler.getMethod().invoke(handler.getController(),paramValues);
        if(result == null||result instanceof  Void){return  null;}
        boolean isModelAndView = handler.getMethod().getReturnType()==GPModelAndView.class;
        if(isModelAndView){
            return  (GPModelAndView) result;
        }
     return  null;
    }

    private Object castStringValue(String value, Class<?> paramType) {
       if(String.class == paramType){
           return  value;
       }
       else if(Integer.class==paramType){
           return Integer.valueOf(value);
       }else if(Double.class == paramType){

       }else{
           if(value!=null){
               return  value;
           }
       }
       return  null;
    }
}
