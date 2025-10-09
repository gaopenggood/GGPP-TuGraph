package com.ggpp.tugraph.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ggpp.tugraph.service.CDKService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cdk")
public class CDKController {

    @Autowired
    private CDKService service;

    @PostMapping("/xjskp")
    public void insertCdk1(@RequestBody JsonNode params) {
        service.insertCdk1(params);
    }
}
