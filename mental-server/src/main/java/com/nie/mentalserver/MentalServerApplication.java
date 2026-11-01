package com.nie.mentalserver;

import com.nie.mental.common.properties.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.nie")
@EnableConfigurationProperties(JwtProperties.class)
public class MentalServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentalServerApplication.class, args);
    }

}
