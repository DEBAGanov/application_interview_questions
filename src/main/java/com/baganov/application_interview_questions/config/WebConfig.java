package com.baganov.application_interview_questions.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Перенаправляем все не-API запросы на index.html
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/{path:^(?!api|static).*$}/**").setViewName("forward:/index.html");
    }
}