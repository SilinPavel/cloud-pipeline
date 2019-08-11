package com.epam.pipeline.adaptor.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class TesAdaptorsController {

    public static void main(String[] args) {
        SpringApplication.run(TesAdaptorsController.class, args);
    }

    @GetMapping("/v1/tasks/service-info")
    public String list() {
        return "OK";
    }
}
