package com.example.finalproject;

import com.example.finalproject.global.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinalProjectApplication {

    public static void main(String[] args) {
        EnvLoader.loadDotEnv();
        SpringApplication.run(FinalProjectApplication.class, args);
    }
}
