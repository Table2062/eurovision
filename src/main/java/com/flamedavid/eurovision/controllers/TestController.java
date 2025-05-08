package com.flamedavid.eurovision.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping("/")
    public String home() {
        return "App is running";
    }

    @GetMapping("/hello")
    public String test() {
        return "Greetings from Davide Figuccia!";
    }
}
