package com.ggpp.tugraph.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.JsonNode;
import com.ggpp.tugraph.service.MainService;
import com.ggpp.tugraph.service.TuGraphService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
        int i = 0;
        for(Currency c : cList) {
            if("USD".equals(c.getCurrencyCode())) {
                log.info("222");
            }
            if(c.getDefaultFractionDigits() < 0) {
                log.info("货币："+c.getDisplayName()+"代码："+c.getCurrencyCode()+",符号："+c.getSymbol());
                i++;
            }
        }
        log.info("共有"+i+"种精度小于0");
    }

    @PostMapping("jsonInit")
    public void formatterJson(@RequestBody JsonNode json) {
        Long comId = 1L;
        for(JsonNode province : json){
            //省一级
            Long id = IdWorker.getId();
            String name = province.get("name").asText();
            String code = province.get("code").asText();
            if(!province.has("city")) {
                continue;
            }
            for(JsonNode city : province.get("city")){
                Long cityId = IdWorker.getId();
                String cityCode = city.get("code").asText();
                String cityName = city.get("name").asText();
                if(!province.has("area")) {
                    continue;
                }
                for(JsonNode area : city.get("area")){
                    Long areaId = IdWorker.getId();
                    String areaCode = area.get("code").asText();
                    String areaName = area.get("name").asText();
                }
            }
        }
    }

    @PostMapping("/t2png")
    public void text2Png(@RequestParam String str) {
        service.doStr2Png(str);
    }
}
