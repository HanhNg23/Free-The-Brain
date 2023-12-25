package com.freethebrain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebMvcConfig implements WebMvcConfigurer {

    private final long MAX_AGE_SECS = 3600;

    @Value("${app.cors.allowedOrigins}")
    private String[] allowedOrigins;

//   This class configure "global" cross-origin request (request from different domain) processing. 
//   The configured `CORSmappings` apply to annotated
//   controllers, functional endpoints, and static resources.
//   setup new value via the class CorsRegistration and CorsConfiguration class

    // Let enabLe CORS so frontend client can access the APIs from a different
    // origin/domain which differ from your app domain and allow to access our application resources
    @Override
    public void addCorsMappings(CorsRegistry registry) {
	registry
		.addMapping("/**")
		.allowedOrigins(allowedOrigins)
		.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
		.allowedHeaders("*") //accept all headers come from the request
		.allowCredentials(true)
		.maxAge(MAX_AGE_SECS);

    }
}
