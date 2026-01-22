package com.auggie.student_server.config;

import com.auggie.student_server.interceptor.AdminInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Auther: auggie
 * @Date: 2024/01/01
 * @Description: WebMvcConfig - Web MVC 配置
 * @Version 1.0.0
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/student/login", "/teacher/login");
    }
}
