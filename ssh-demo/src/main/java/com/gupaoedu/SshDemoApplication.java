package com.gupaoedu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
public class SshDemoApplication {
    // 注意是mybatis数据库
    public static void main(String[] args) {
        SpringApplication.run(SshDemoApplication.class, args);
    }

}
