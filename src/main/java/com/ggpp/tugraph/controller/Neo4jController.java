package com.ggpp.tugraph.controller;

import com.ggpp.tugraph.service.Neo4jService;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.EagerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public EagerResult findVisualization() {
        return service.findVisualization();
    }

    @GetMapping("/nodes")
    public List<String> findNodeList() {
        return service.findNodeList();
    }

    @GetMapping("/keys")
    public List<String> findNodeKeysByLabel(String label) {
        return service.findNodeKeysByLabel(label);
    }

    @GetMapping("/text")
    public Object queryText() {
        return service.queryText();
    }

}
