package com.agonylua.smartkitchen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableIntegration
@EnableScheduling
@IntegrationComponentScan
public class SmartKitchenApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartKitchenApplication.class, args);
    }

}
