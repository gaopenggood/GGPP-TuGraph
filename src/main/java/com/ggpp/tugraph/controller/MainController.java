package com.ggpp.tugraph.controller;

import com.ggpp.tugraph.service.MainService;
import com.ggpp.tugraph.service.TuGraphService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @Resource
    private MainService service;

    @GetMapping
    public String testGet() {
        Object o = service.getDataFromDB();
        return "GET";
    }

    @PostMapping
    public String testPost() {
        return "POST";
    }

    @PostMapping("/tuGraph")
    public void doTuGraphTest() {
        service.doTuGraphTest();
    }
}
