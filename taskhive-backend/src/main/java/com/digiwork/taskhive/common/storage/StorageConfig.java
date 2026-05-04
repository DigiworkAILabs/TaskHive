package com.digiwork.taskhive.common.storage;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StorageConfig implements WebMvcConfigurer {

    @Value("${storage.local.upload-dir:./uploads}")
    private String uploadDir;

    // Static file serving removed for security. Downloads are mapped via explicit controllers.

    @Bean
    public FilterRegistrationBean<Filter> uploadContentDispositionFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter((request, response, chain) -> {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("Content-Disposition", "attachment");
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            chain.doFilter(request, response);
        });
        registration.addUrlPatterns("/uploads/*");
        return registration;
    }
}
