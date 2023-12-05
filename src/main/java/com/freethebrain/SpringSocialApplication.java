package com.freethebrain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.freethebrain.config.AppProperties;

//note: to let the "app" properties in yaml file to mapp with the properties in AppProperties class
//You have to trigger the mappings by EnableConfigurationPropereties in 

@EnableConfigurationProperties(AppProperties.class)
@SpringBootApplication
public class SpringSocialApplication {

    public static void main(String[] args) {
	SpringApplication.run(SpringSocialApplication.class, args);
    }

}
