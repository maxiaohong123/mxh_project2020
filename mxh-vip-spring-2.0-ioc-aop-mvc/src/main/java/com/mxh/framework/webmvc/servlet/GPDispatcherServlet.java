package com.mxh.framework.webmvc.servlet;

import com.mxh.framework.annotation.GPController;
import com.mxh.framework.annotation.GPRequestMapping;
import com.mxh.framework.annotation.GPRequestParam;
import com.mxh.framework.context.GPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

//注意：这是IOC项目，所以该servlet还需要继续改造才能正常运行。
public class GPDispatcherServlet extends HttpServlet {

    //上下文
    private GPApplicationContext applicationContext;

    //IoC容器，key默认是类名首字母小写，value就是对应的实例对象
    private Map<String,Object> ioc = new HashMap<String,Object>();

    private Map<String,Method> handlerMapping = new HashMap<String, Method>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception Detail:"+Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp)  throws Exception{

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!");
        }

        Map<String,String[]> params = req.getParameterMap();
        Method method = this.handlerMapping.get(url);
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] paramValues = new Object[paramTypes.length];

        for(int i=0;i<paramTypes.length;i++){
            Class paramType = paramTypes[i];
            if(paramType==HttpServletRequest.class){
                paramValues[i] = req;
            }else if(paramType==HttpServletResponse.class){
                paramValues[i] = resp;
            }else if(paramType==String.class){
                Annotation[][] pa = method.getParameterAnnotations();
                for(int j=0;j<pa.length;j++){
                    for(Annotation a:pa[i]){
                        if(a instanceof GPRequestParam){
                            String paramName = ((GPRequestParam) a).value();
                            if(!"".equals(paramName.trim())){
                                String value = Arrays.toString(params.get(paramName)).replaceAll("\\[|\\]","").replaceAll("\\s+",",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }
            }
        }

        //获取参数名称为name的参数，暂时硬编码
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        //
        method.invoke(ioc.get(beanName),paramValues);

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、初始化spring的核心IOC容器
        applicationContext = new GPApplicationContext(config.getInitParameter("contextConfigLocation"));

        //===================MVC部分=========================
        //5、初始化HandlerMapping
        doInitHandlerMapping();

        System.out.println("GP Spring framework is init.");

    }

    //完成url与Method的映射关系
    private void doInitHandlerMapping() {
        if(ioc.isEmpty()){
            return;
        }
        //1、思考：要完成url与Method的映射关系，哪些类需要处理呢？当然是标注@Controller的类
        for(Map.Entry<String,Object> entry:ioc.entrySet()){
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(GPController.class)){
                continue;
            }
            //2、获取写在类上的url路径
            String baseUrl = "";
            if(clazz.isAnnotationPresent(GPRequestMapping.class)){
                GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //3、默认获取所有public方法
            for(Method method:clazz.getMethods()){
                if(!method.isAnnotationPresent(GPRequestMapping.class)){
                    continue;
                }
                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);
                String url = ("/"+baseUrl+"/"+requestMapping.value()).replaceAll("/+","/");
                handlerMapping.put(url,method);
                System.out.println("Mapped "+url+" ,"+method);
            }
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
//        if(chars[0] > )
        chars[0] += 32;
        return String.valueOf(chars);
    }
}


