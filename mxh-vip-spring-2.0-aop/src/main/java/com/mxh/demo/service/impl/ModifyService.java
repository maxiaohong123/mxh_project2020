package com.mxh.demo.service.impl;


import com.mxh.demo.service.IModifyService;
import com.mxh.framework.annotation.GPService;

/**
 * 增删改业务
 * @author Tom
 *
 */
@GPService
public class ModifyService implements IModifyService {

	/**
	 * 增加
	 */
	public String add(String name,String addr)  throws  Exception{
		//return "modifyService add,name=" + name + ",addr=" + addr;
		 throw new Exception("这是tom故意抛出的异常");
	}

	/**
	 * 修改
	 */
	public String edit(Integer id,String name) {
		return "modifyService edit,id=" + id + ",name=" + name;
	}

	/**
	 * 删除
	 */
	public String remove(Integer id) {
		return "modifyService id=" + id;
	}
	
}
