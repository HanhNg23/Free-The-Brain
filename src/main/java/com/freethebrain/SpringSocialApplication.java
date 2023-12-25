package com.freethebrain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.freethebrain.config.AppProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

//To bind all the prefixed "app" properties in application.yml into Java Class (POJO Class)
//you have to trigger the mappings by EnableConfigurationPropereties like following: 
@EnableConfigurationProperties(AppProperties.class)
@SpringBootApplication
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Free The Brain API",
                version = "1.0.0"
        ),
        servers = {
            @Server(url = "http://localhost:8080")
        }
)
@SecurityScheme(
		  name = "Bearer Authentication",
		  type = SecuritySchemeType.HTTP,
		  bearerFormat = "JWT",
		  scheme = "bearer"
		  
		  
)
public class SpringSocialApplication {
    public static void main(String[] args) {
	SpringApplication.run(SpringSocialApplication.class, args);
    }

}
