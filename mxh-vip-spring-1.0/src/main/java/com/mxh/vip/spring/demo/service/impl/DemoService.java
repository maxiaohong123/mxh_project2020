package com.mxh.vip.spring.demo.service.impl;

import com.mxh.vip.spring.demo.service.IDemoService;
import com.mxh.vip.spring.framework.annotation.GPService;

/**
 * 核心业务逻辑
 */
@GPService
public class DemoService implements IDemoService {

	public String get(String name) {
		return "My name is " + name + ",from service.";
	}

}
