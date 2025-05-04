package com.flamedavid.eurovision.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/")
    public String home() {
        return "App is running";
    }

    @GetMapping("/test")
    public String test() {
        return "Greetings from Davide Figuccia!";
    }
}
