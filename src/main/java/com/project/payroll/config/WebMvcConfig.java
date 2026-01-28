package com.project.payroll.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL /uploads/** to the physical folder uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
