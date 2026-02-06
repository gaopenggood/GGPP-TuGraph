package com.ggpp.tugraph.controller;

import com.ggpp.tugraph.service.NewTechService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/newTech")
@RequiredArgsConstructor
public class NewTechController {

    private final NewTechService service;

    @GetMapping("/NLP")
    public Object doNLPTest() {
        return service.doNLPTest();
    }
}
