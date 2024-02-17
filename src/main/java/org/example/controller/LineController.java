package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LineController {

    @GetMapping("/")
    public String testEndpoint() {
        return "The service is up and running!";
    }

    @PostMapping("/test")
    public String testEndpoint2() {
        return "The service is up and running!";
    }

}
