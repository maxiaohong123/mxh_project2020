package com.mxh.framework.v3;

import com.mxh.framework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这是手写spring1.0中的v3版本：与v2相比，HandlerMapping中的url支持正则匹配。
 */
public class XHServlet extends HttpServlet {

    //保存application.properties配置文件中的内容
    private Properties contextConfig = new Properties();

    //保存扫描到的类名
    private List<String> classNames = new ArrayList<String>();

    //IOC容器
    //这就是传说中的IOC容器，为了简化程序，暂不考虑ConcurrentHashMap,主要关注思想
    private Map<String,Object> ioc = new HashMap<String, Object>();

    //保存url和Method的对应关系 ：v2版本
//    private Map<String,Method> handlerMapping = new HashMap<String, Method>();
    //保存url和Method的对应关系，method用一个Handler封装起来。
     private List<Handler> handlerMapping = new ArrayList<Handler>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatcher(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception"+ Arrays.toString(e.getStackTrace()));
        }
    }

    //doPost方法中，采用了委派模式，具体逻辑在doDispatch()方法中。
    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {

      //1、获取浏览器输入的url,根据url获取一个Handler
           Handler handler = getHandler(req);
           if(handler == null){
               resp.getWriter().write("404 Not Found");
               return;
           }

     //2、获取方法的参数列表(带注解的 @RequestParam)
        Class<?>[] paramTypes = handler.method.getParameterTypes();
        Object[] paramValues = new Object[paramTypes.length];
        Map<String,String[]> params = req.getParameterMap();
        for(Map.Entry<String,String[]> param:params.entrySet()){
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","").replaceAll("\\s",",");
            //如果找到匹配的对象，则开始填充参数值
            if(!handler.paramIndexMapping.containsKey(param.getKey())){continue;}
            int index = handler.paramIndexMapping.get(param.getKey());
            paramValues[index] = convert(paramTypes[index],value);
        }

        //3、获取方法中的request和response参数
        int reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
        paramValues[reqIndex] = req;
        int respIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
        paramValues[respIndex] = resp;
        handler.method.invoke(handler.controller,paramValues);
    }

    /**
     * @Description:在v1的版本上进行了优化，采用了常用的设计模式(工厂模式，单例模式，委派模式，策略模式)，将init方法中的代码进行模块封装
     * 先搭框架，再实现细节。
     * @param config
     * @throws ServletException
     */

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2、扫描配置包下相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        // =================IOC部分==================
        //3、初始化扫描到的类，并且存入IOC容器中
        doInstance();
        // =================DI部分==================
        //4、完成自动化的依赖注入
        doAutowired();
        // =================MVC部分==================
        //5、初始化HandlerMapping
        doInitHandlerMapping();

        System.out.print("GP MVC Framework is init");
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
                String regex = ("/"+baseUrl+requestMapping.value()).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                handlerMapping.add(new Handler(pattern,entry.getValue(),method));
                System.out.println("Mapped "+regex+" ,"+method);
            }
        }
    }

    //完成依赖注入
    private void doAutowired() {
        if(ioc.isEmpty()){
            return;
        }

        //1、循环IOC容器，进行依赖注入
        for(Map.Entry<String,Object> entry:ioc.entrySet()){
            //1.1 getDeclaredFields 拿到类的public/private/default方法
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for(Field field:fields){
                //1.2 思考：什么样的字段才需要依赖注入呢？标注@Autowired 或@Resource的，才需要依赖注入。
                if(!field.isAnnotationPresent(GPAutowired.class)){
                    continue;
                }
                GPAutowired autowired = field.getAnnotation(GPAutowired.class);
                //1.3 分析：如果用户没有自定义beanName,默认就根据类型注入；
                String beanName = autowired.value().trim();
                if("".equals(beanName)){
                    //用这个key去,ioc中去拿到对应的实例
                    //field.getType().getName():获取该字段的全类名,比如：com.mxh.demo.service.IDemoService
                    beanName = field.getType().getName();
                }
                //如果是public以外的修饰符，只要加了@Autowired注解，都要强制赋值。暴力访问。
                field.setAccessible(true);
                try{
                    field.set(entry.getValue(),ioc.get(beanName));
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }


    //实例化，它就是工厂模式的具体实现
    private void doInstance() {
        if(classNames.isEmpty()){return;}
        try{
            for(String className:classNames){
                //1、思考：不是所有的类都初始化，只有标注@Controller、@Service、@Componment的类才初始化，@Component就不实现了，主要了解思想。
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(GPController.class)){
                    Object instance = clazz.newInstance();
                    // getSimpleName():获取类名称，比如：DemoAction.
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,instance);

                }else if(clazz.isAnnotationPresent(GPService.class)){
                    //1、注入IOC容器时，默认采用类名首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    //2、判断是否有自定义的beanName
                    GPService service = clazz.getAnnotation(GPService.class);
                    if(!"".equals(service.value())){
                        beanName = service.value();
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);

                    //3、处理它的接口如何注入IOC容器中，此处只支持一个接口一个实现类。
                    for(Class<?> i : clazz.getInterfaces()){
                        //i.getName():获取全类名，比如：com.mxh.demo.service.IDemoService
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The beanName is exists");
                        }
                        ioc.put(i.getName(),instance);
                    }

                }else{
                    continue;
                }

            }

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void doLoadConfig(String contextConfigLocation) {
        //表示从当前类路径下找到spring配置文件所在的路径，将它读出来放在Properties对象中
        //相当于 scanPackage=com.mxh.demo 从文件中保存到了内存中
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try{
            contextConfig.load(is);
        }
        catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null!=is){
                try{
                    is.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    }

    private void doScanner(String scanPackage) {

        //scanPackage=com.mxh.demo，存储的是包路径，它下面有各种各样的文件,.class  .xml  .properties等，将“.”替代为"/"就可以了。
        URL url =  this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for(File file:classPath.listFiles()){

            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else{
                if(!file.getName().endsWith(".class")){
                    continue;
                }
                classNames.add(scanPackage+"."+file.getName().replace(".class",""));
            }
        }
    }

    //将类名首字母转换为小写
    //如果类名本身是小写，此方法会出问题。但实际工作中，谁会将类名首字母小写呢。
    private String toLowerFirstCase(String simpleName){
        char [] chars = simpleName.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }

    private  class Handler{
        protected  Object controller; //方法对应的实例
        protected  Method method;//方法
        protected Pattern pattern;//${}url占位符解析
        protected Map<String,Integer> paramIndexMapping;//参数顺序
        protected  Handler(Pattern pattern,Object controller,Method method){
            this.controller = controller;
            this.method = method;
            this.pattern = pattern;

            paramIndexMapping = new HashMap<String, Integer>();
            putParamIndexMapping(method);
        }

        //1、提取方法中带注解的参数
        private  void putParamIndexMapping(Method method){
            Annotation[][] pa = method.getParameterAnnotations();
            for(int i=0;i<pa.length;i++){
                for(Annotation a:pa[i]){
                    if(a instanceof GPRequestParam){
                        String paramName = ((GPRequestParam) a).value();
                        if(!"".equals(paramName.trim())){
                            paramIndexMapping.put(paramName,i);
                        }
                    }
                }
            }

            //2、提取方法中的request和response参数
            Class<?> [] paramsTypes = method.getParameterTypes();
            for(int i=0;i<paramsTypes.length;i++){
                Class<?> type = paramsTypes[i];
                if(type==HttpServletRequest.class||type==HttpServletResponse.class){
                    paramIndexMapping.put(type.getName(),i);
                }
            }
        }
    }

    //根据请求url在handlerMapping中获取一个Handler
    private  Handler getHandler(HttpServletRequest req) throws Exception{
        if(handlerMapping.isEmpty()){return  null;}
        // 获取浏览器访问路径
        String url = req.getRequestURI();
        // req.getContextPath ：指项目的根路径名称，idea默认是没有根路径的，需要配置ApplicationContext
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
        for(Handler handler:handlerMapping){
            try{
                Matcher matcher = handler.pattern.matcher(url);
                if(!matcher.matches()){continue;}
                return handler;
            }catch (Exception e){
                throw  e;
            }
        }
        return  null;
    }

    //url参数类型转换：url传给后端的参数都是string类型的,HTTP是基于字符串协议，只需要把字符串转换为任意类型即可。
    private  Object convert(Class<?> type,String value){
        if(Integer.class==type){
            return  Integer.valueOf(value);
        }
        //注意：此处还有double类型、javaBean类型的，采用策略模式实现。
        return value;
    }

}
