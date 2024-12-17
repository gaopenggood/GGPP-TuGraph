package com.ggpp.tugraph.service;

import cn.hutool.core.collection.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class Neo4jService {

    private final Neo4jClient neo4jClient;

    // 通过构造函数注入Neo4jClient
    @Autowired
    public Neo4jService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public Object findVisualization() {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "GGpp1993@"));
        var result = driver.executableQuery("CALL db.schema.visualization()")
                .withConfig(QueryConfig.builder().withDatabase("neo4j").build())
                .execute();

        var records = result.records();
        return records;
    }

    public Object findNodeKeysByLabel(String label) {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "GGpp1993@"));
        EagerResult result = driver.executableQuery("MATCH (n:"+label+") WITH DISTINCT keys(n) AS propertyKeys RETURN propertyKeys ")
                .withConfig(QueryConfig.builder().withDatabase("neo4j").build())
                .execute();

        List<Record> records = result.records();
        List<String> keyList = new ArrayList<>();
        for(Record record : records){
            for(Object value : record.get(0).asList()){
                keyList.add(String.valueOf(value));
            }
        }
        keyList = keyList.stream().distinct().collect(Collectors.toList());
        return keyList;
    }
}
