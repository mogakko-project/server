package com.example.mogakko.global.config;

import com.example.mogakko.global.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
//@EnableWebMvc
//@ComponentScan(basePackages = {"com.spring.jwt.controller", "com.spring.jwt.interceptor"}) // Interceptor도 스캔
public class ServletContext implements WebMvcConfigurer {

    // Interceptor 등록
    @Autowired
    LoginInterceptor loginInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) { // client에서 header추출이 가능하도록 하기 위해 등록
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://mogakko-project.github.io/client/")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) { // 인터셉터 등록
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // Interceptor가 적용될 경로
                .excludePathPatterns(
                        "/api/users/login",
                        "/api/users/register",
                        "/api/users/username-redundancy",
                        "/api/users/auth",
                        "/error",
                        "/api/posts/type/{postType}",
                        "/api/posts/{postId}",
                        "/api/languages",
                        "/api/locations",
                        "/api/occupations",
                        "/api/posts/{postId}/comments"
                );
    }

}
