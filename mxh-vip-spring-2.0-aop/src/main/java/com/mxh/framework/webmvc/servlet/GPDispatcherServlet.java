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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GPDispatcherServlet extends HttpServlet {

    //上下文【1、加载配置文件  2、扫描配置文件的包  3、解析配置文件中的配置为集合  4、将集合变为BeanDefinition 5、注册BeanDefinition 6、依赖注入】
    private GPApplicationContext applicationContext;
    //初始化时存储url与Method封装好的HandlerMapping
    private List<GPHandlerMapping> handlerMappings = new ArrayList<GPHandlerMapping>();
    private Map<GPHandlerMapping,GPHandlerAdapter> handlerAdapters = new HashMap<GPHandlerMapping, GPHandlerAdapter>();
    private List<GPViewResolver> viewResolvers = new ArrayList<GPViewResolver>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                processDispatchResult(req,resp,new GPModelAndView("500"));
            } catch (Exception e1) {
                e1.printStackTrace();
                resp.getWriter().write("500 Exception Detail:"+Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //1、通过url获得一个HandlerMapping。
        GPHandlerMapping handler = getHandler(req);

//        String url = req.getRequestURI();
//        String contextPath = req.getContextPath();
//        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
//
//
//        if(!this.handlerMapping.containsKey(url)){
//            resp.getWriter().write("404 Not Found!");
//        }

        if(handler==null){
            processDispatchResult(req,resp,new GPModelAndView("404"));
            return;
        }

        //2、根据一个HandlerMapping获得一个HandlerAdapter;
        GPHandlerAdapter ha = getHandlerAdapter(handler);

        //3、解析某一个方法的形参和返回值以后，统一封装为ModelAndView对象。
        GPModelAndView mv = ha.handle(req,resp,handler);

        //4、把ModelAndView变成一个ViewResolver。
        processDispatchResult(req,resp,mv);
    }

    //责任：根据Handler获得一个HandlerAdapter，用于动态解析方法类型。因为：url传递的所有参数都是字符串类型的。
    private GPHandlerAdapter getHandlerAdapter(GPHandlerMapping handler) {
        if(this.handlerAdapters.isEmpty()){return  null;}
        return  this.handlerAdapters.get(handler);
    }

    // 责任：统一封装的返回结果,将ModelAndView解析为ViewResolver
    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, GPModelAndView mv) throws Exception{
       if(null ==mv){
           return;
       }
       if(this.viewResolvers.isEmpty()){
           return;
       }
       for(GPViewResolver viewResolver:this.viewResolvers){
           GPView view = viewResolver.resolveViewName(mv.getViewName());
           //直接往浏览器输出了。
           view.render(mv.getModel(),req,resp);
           return;  //注意：如果此处不加return,会把一个页面渲染viewResolvers.size次。
       }
    }

    //责任：根据请求获得一个HandlerMapping。【1、组装请求url  2、用初始化好的HandlerMappings中的任意一个进行正则匹配】
    private GPHandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){
            return  null;
        }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        for(GPHandlerMapping mapping:handlerMappings){
            Matcher matcher = mapping.getPattern().matcher(url);
            if(!matcher.matches()){
                continue;
            }
            return  mapping;
        }
        return  null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、初始化spring的核心IOC容器
        applicationContext = new GPApplicationContext(config.getInitParameter("contextConfigLocation"));

        //2、初始化九大组件
        initStrategies(applicationContext);
        //===================MVC部分=========================


        System.out.println("GP Spring framework is init.");

    }

    //责任：初始化九大组件
    private void initStrategies(GPApplicationContext context) {
//        //多文件上传的组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
//        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
//        //FlashMap管理器
//        initFlashMapManager(context);
    }

    //责任：初始化 HandlerMapping,也就是url与Method的对应关系。
    private void initHandlerMappings(GPApplicationContext context) {
        if(this.applicationContext.getBeanDefinitionCount()==0){
            return;
        }
        //1、思考：要完成url与Method的映射关系，哪些类需要处理呢？当然是标注@Controller的类
        System.out.println("MXH:"+this.applicationContext.getBeanDefinitionNames().length);
        for(String beanName:this.applicationContext.getBeanDefinitionNames()){
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = this.applicationContext.getBean(beanName).getClass();
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
                String regex = ("/"+baseUrl+"/"+requestMapping.value()).replaceAll("\\*",".*").replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new GPHandlerMapping(pattern,method,instance));
                System.out.println("Mapped "+regex+" ,"+method+"************");
            }
        }
    }

    private void initHandlerAdapters(GPApplicationContext context) {
      for(GPHandlerMapping handlerMapping:handlerMappings){
          this.handlerAdapters.put(handlerMapping,new GPHandlerAdapter());
      }
    }

    private void initViewResolvers(GPApplicationContext context) {
       String templateRoot = context.getConfig().getProperty("templateRoot"); //注意：此处一定要是templateRoot，而非layouts

       String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

       File templateRootDir = new File(templateRootPath);
       for(File file:templateRootDir.listFiles()){
          this.viewResolvers.add(new GPViewResolver(templateRoot));
       }

    }





    //完成url与Method的映射关系
//    private void doInitHandlerMapping() {
//        if(this.applicationContext.getBeanDefinitionCount()==0){
//            return;
//        }
//        //1、思考：要完成url与Method的映射关系，哪些类需要处理呢？当然是标注@Controller的类
//        for(String beanName:this.applicationContext.getBeanDefinitionNames()){
//            Object instance = applicationContext.getBean(beanName);
//            Class<?> clazz = this.applicationContext.getBean(beanName).getClass();
//            if(!clazz.isAnnotationPresent(GPController.class)){
//                continue;
//            }
//            //2、获取写在类上的url路径
//            String baseUrl = "";
//            if(clazz.isAnnotationPresent(GPRequestMapping.class)){
//                GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
//                baseUrl = requestMapping.value();
//            }
//
//            //3、默认获取所有public方法
//            for(Method method:clazz.getMethods()){
//                if(!method.isAnnotationPresent(GPRequestMapping.class)){
//                    continue;
//                }
//                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);
//                String regex = ("/"+baseUrl+"/"+requestMapping.value()).replaceAll("\\*",".*").replaceAll("/+","/");
//                Pattern pattern = Pattern.compile(regex);
//                handlerMappings.add(new GPHandlerMapping(pattern,method,instance));
//                System.out.println("Mapped "+regex+" ,"+method);
//            }
//        }
//    }

//    private String toLowerFirstCase(String simpleName) {
//        char [] chars = simpleName.toCharArray();
////        if(chars[0] > )
//        chars[0] += 32;
//        return String.valueOf(chars);
//    }
}


