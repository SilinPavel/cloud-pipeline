package com.epam.pipeline.adaptor.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TesAdaptorsController {

    @GetMapping("/v1/tasks/service-info")
    public String list() {
        return "OK";
    }
}
