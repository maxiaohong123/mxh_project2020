package com.gupaoedu.dao;

import com.gupaoedu.domain.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogDao extends JpaRepository<Blog, Integer> {

}