package com.ggpp.tugraph.service;

import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DbService {

    @Resource
    private JdbcTemplate jdbc;

    public List<Map<String, Object>> doGet(String sql) {
        return jdbc.queryForList(sql);
    }
}
