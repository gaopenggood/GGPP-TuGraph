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
import org.springframework.web.multipart.MultipartFile;

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
    public void text2Png() {
        String str = "data:image/png;base64,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";
        service.doStr2Png(str);
    }

    @PostMapping("/f2png")
    public void file2Png(@RequestBody MultipartFile file) {
        service.doFile2Png(file);
    }

    @PostMapping("/file2Local")
    public void file2Local(@RequestBody MultipartFile file) {
        service.doFile2Local(file);
    }

    @PostMapping("/changeFilePath")
    public void changeFilePath() {
        service.changeFilePath();
    }

    @PostMapping("/excel")
    public void getExcelData(@RequestBody MultipartFile file) {
        service.getExcelData(file);
    }
}
