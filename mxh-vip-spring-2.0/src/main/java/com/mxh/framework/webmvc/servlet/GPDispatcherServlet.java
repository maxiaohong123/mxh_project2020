package com.mxh.framework.webmvc.servlet;

import com.mxh.framework.annotation.GPController;
import com.mxh.framework.annotation.GPRequestMapping;
import com.mxh.framework.context.GPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
        doDispatch(req,resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {


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
}


