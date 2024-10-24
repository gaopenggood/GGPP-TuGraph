package com.ggpp.tugraph.controller;

import com.ggpp.tugraph.service.MainService;
import com.ggpp.tugraph.service.TuGraphService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.Set;

@Slf4j
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

    @GetMapping("/device")
    public void deviceType(HttpServletRequest request, HttpServletResponse response) {
        String userAgent = request.getHeader("User-Agent");
        String deviceType = detectDeviceType(userAgent);
        log.info("设备："+deviceType);
    }

    private String detectDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        // Simple logic to detect mobile devices
        if (userAgent.toLowerCase().contains("mobile")
                || userAgent.toLowerCase().contains("android")
                || userAgent.toLowerCase().contains("iphone")
                || userAgent.toLowerCase().contains("ipad")
                || userAgent.toLowerCase().contains("windows phone")) {
            return "Mobile";
        } else {
            return "Desktop";
        }
    }

    @GetMapping("/iso4217")
    public void iso4217() {
        Set<Currency> cList = Currency.getAvailableCurrencies();
        for(Currency c : cList) {
            if("USD".equals(c.getCurrencyCode())) {
                log.info("222");
            }
        }
        log.info("111");
    }
}
