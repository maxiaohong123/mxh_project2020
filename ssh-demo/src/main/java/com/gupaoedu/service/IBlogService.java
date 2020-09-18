package com.gupaoedu.service;

import com.gupaoedu.domain.Blog;
import java.util.List;

public interface IBlogService {
    public List<Blog> queryBlog();

    public void addBlog(Blog blog);
}
