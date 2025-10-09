package com.ggpp.tugraph.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.ggpp.tugraph.domain.GameCdKey;
import com.ggpp.tugraph.mapper.GameCdKeyMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class CDKService {

    @Autowired
    private GameCdKeyMapper mapper;

    @Transactional
    public void insertCdk1(JsonNode params) {
        List<GameCdKey> eList = mapper.selectList(new LambdaQueryWrapper<GameCdKey>());
        log.info("aaa");
    }
}
