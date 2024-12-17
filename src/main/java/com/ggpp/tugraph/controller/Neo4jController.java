package com.ggpp.tugraph.controller;

import com.ggpp.tugraph.service.Neo4jService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/neo4j")
public class Neo4jController {

    @Autowired
    private Neo4jService service;

    /**
     * 查询数据模型
     */
    @GetMapping("/visualization")
    public Object findVisualization() {
        return service.findVisualization();
    }

    @GetMapping("/keys")
    public Object findNodeKeysByLabel(String label) {
        return service.findNodeKeysByLabel(label);
    }

}
