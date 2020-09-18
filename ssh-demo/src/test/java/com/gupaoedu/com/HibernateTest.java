package com.gupaoedu.com;

import com.gupaoedu.domain.Blog;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class HibernateTest {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        // 默认使用hibernate.cfg.xml
        configuration.configure();
        // 创建Session工厂
        SessionFactory factory = configuration.buildSessionFactory();
        // 创建Session
        Session session = factory.openSession();
        // 获取事务对象
        Transaction transaction = session.getTransaction();
        // 开启事务
        transaction.begin();
        // 把对象添加到数据库中
        Blog blog = new Blog();
        blog.setBid(9000004);
        blog.setName("MySQL从入门到入狱");
        blog.setAuthorId(1001);
        session.save(blog);
        // 提交事务
        transaction.commit();

        // 关闭Session
        session.close();
    }
}