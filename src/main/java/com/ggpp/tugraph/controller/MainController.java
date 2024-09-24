package com.ggpp.tugraph.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping
    public String testGet() {
        return "GET";
    }

    @PostMapping
    public String testPost() {
        return "POST";
    }
}
