package com.ggpp.tugraph.controller;

import com.ggpp.tugraph.service.TuGraphService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/node/person")
public class NodePersonController {

    @Resource
    private TuGraphService service;

    @PostMapping
    public void doText() {
        service.createData();
        // Query data
        service.findMoviesByPersonName("Alice");
    }
}
