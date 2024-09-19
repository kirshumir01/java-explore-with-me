package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"ru.practicum", "ru.practicum.ewm"})
public class ExploreWithMeService {
    public static void main(String[] args) {
        SpringApplication.run(ExploreWithMeService.class, args);
    }
}