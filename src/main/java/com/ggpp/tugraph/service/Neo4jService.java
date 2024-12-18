package com.ggpp.tugraph.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.InternalNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    public EagerResult findVisualization() {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "GGpp1993@"));
        EagerResult result = driver.executableQuery("CALL db.schema.visualization()")
                .withConfig(QueryConfig.builder().withDatabase("neo4j").build())
                .execute();

//        List<Record> records = result.records();
        return result;
    }

    public List<String> findNodeKeysByLabel(String label) {
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

    public List<String> findNodeList() {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "GGpp1993@"));
        EagerResult result = driver.executableQuery("CALL db.schema.visualization()")
                .withConfig(QueryConfig.builder().withDatabase("neo4j").build())
                .execute();
        List<Record> records = result.records();
        List<Object> list = records.get(0).get(0).asList();
        List<String> reList = new ArrayList<>();
        for(Object obj : list){
            InternalNode node = (InternalNode) obj;
            reList.addAll(node.labels().stream().toList());
        }
        reList = reList.stream().distinct().collect(Collectors.toList());
        return reList;
    }

    public Object queryText() {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "GGpp1993@"));
        EagerResult result = driver.executableQuery("Match (c:Com)-[cc:Com_Com]->(child)<-[dc:Dept_Com]-(d:Dept)<-[pd:Post_Dept]-(p:Post) where c.name='上海普华科技发展股份有限公司' return c,cc,child,dc,d,pd,p")
                .withConfig(QueryConfig.builder().withDatabase("neo4j").build())
                .execute();
        return result;
    }
}
