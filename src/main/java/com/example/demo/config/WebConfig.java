package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Aplica a TODAS las rutas bajo /api/
                        .allowedOrigins("http://127.0.0.1:5501", "http://localhost:5501") // Permite estos orígenes
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite estos métodos HTTP
                        .allowedHeaders("*") // Permite todas las cabeceras
                        .allowCredentials(true); // Permite el envío de credenciales (como tokens)
            }
        };
    }
}