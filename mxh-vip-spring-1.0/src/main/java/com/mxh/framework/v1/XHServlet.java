package com.mxh.framework.v1;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class XHServlet extends HttpServlet {

    private Map<String,Object> mapping = new HashMap<String,Object>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatcher(req,resp);
    }


    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) {


    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try {
        Properties configContext = new Properties();
        is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
        configContext.load(is);
        String scanPackage = configContext.getProperty("scanPackage");
        doScanner(scanPackage);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doScanner(String scanPackage) {

        URL url =  this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File classDir = new File(url.getFile());
        for(File file:classDir.listFiles()){

            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else{
                if(!file.getName().endsWith(".class")){
                    continue;
                }
            }

            String className = scanPackage+"."+file.getName().replaceAll(".class","");
            mapping.put(className,null);
        }
    }
}
