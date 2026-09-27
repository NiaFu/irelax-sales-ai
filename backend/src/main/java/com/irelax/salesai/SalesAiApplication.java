package com.irelax.salesai;

import com.irelax.salesai.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(AppProperties.class)
public class SalesAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalesAiApplication.class, args);
    }
}
