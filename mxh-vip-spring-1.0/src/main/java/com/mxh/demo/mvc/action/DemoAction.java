package com.mxh.demo.mvc.action;

import com.mxh.demo.service.IDemoService;
import com.mxh.framework.annotation.GPAutowired;
import com.mxh.framework.annotation.GPController;
import com.mxh.framework.annotation.GPRequestMapping;
import com.mxh.framework.annotation.GPRequestParam;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



//虽然，用法一样，但是没有功能
@GPController
@GPRequestMapping("/demo")
public class DemoAction {


	@GPAutowired private IDemoService demoService;

	@GPRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @GPRequestParam("name") String name){
		String result = demoService.get(name); //适合v2、v3版本。
//		String result = "My name is " + name; //适合v1、v2、v3版本
		System.out.println(result);
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@GPRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@GPRequestParam("a") Integer a, @GPRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@GPRequestMapping("/sub")
	public void sub(HttpServletRequest req, HttpServletResponse resp,
					@GPRequestParam("a") Double a, @GPRequestParam("b") Double b){
		try {
			resp.getWriter().write(a + "-" + b + "=" + (a - b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@GPRequestMapping("/remove")
	public String  remove(@GPRequestParam("id") Integer id){
		return "" + id;
	}

}
