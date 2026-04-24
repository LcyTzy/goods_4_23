package com.zhantu.config;

import com.zhantu.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/captcha",
                        "/api/category/tree",
                        "/api/product/**",
                        "/api/admin/login",
                        "/api/coupon/list",
                        "/api/banner/list",
                        "/api/epc/**",
                        "/api/service-order/**",
                        "/api/admin/supplier/list",
                        "/api/admin/purchase-order/**",
                        "/api/admin/inventory/**",
                        "/api/admin/service-order/**",
                        "/api/admin/vehicle/**",
                        "/api/admin/vehicle/part-relation/**",
                        "/api/vehicle/decode"
                );
    }
}
