package com.gupaoedu;

import com.gupaoedu.crud.bean.Employee;
import com.gupaoedu.crud.rowmapper.BaseRowMapper;
import com.gupaoedu.crud.rowmapper.EmployeeRowMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @Author: qingshan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class EmployeeRowMapperTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    List<Employee> list;

    @Test
    public void EmployeeTest() {
        // EmployeeRowMapper需要将全部属性一个一个写出来，而不通用
//        list = jdbcTemplate.query("select * from tbl_emp", new EmployeeRowMapper());
        //BaseRowMapper 实现一个通用的映射。通过反射。
         list = jdbcTemplate.query(" select * from tbl_emp" ,new BaseRowMapper(Employee.class));
        System.out.println(list);
    }
}
