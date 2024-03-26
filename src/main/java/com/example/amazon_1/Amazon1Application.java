package com.example.amazon_1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Amazon1Application {

    public static void main(String[] args) {
        SpringApplication.run(Amazon1Application.class, args);
    }

}
