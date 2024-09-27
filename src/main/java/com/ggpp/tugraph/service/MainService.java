package com.ggpp.tugraph.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ggpp.tugraph.domain.BaseUser;
import com.ggpp.tugraph.mapper.BaseUserMapper;
import jakarta.annotation.Resource;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;

@Service
public class MainService {

    @Resource
    private BaseUserMapper baseUserMapper;
    public Object getDataFromDB() {
        List<BaseUser> list = baseUserMapper.selectList(new LambdaQueryWrapper<BaseUser>()
                .eq(BaseUser::getId, 1L));
        return list;
    }

    @Transactional
    public void doTuGraphTest() {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "GGpp1993@"));
        try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            // 在自动提交模式下，每个查询都会自动在一个事务中执行并提交
            // 打印结果（可选）
            session.run("CALL db.dropDB()");
            session.run("CALL db.createVertexLabel('person', 'id' , 'id' ,INT32, false, 'name' ,STRING, false)");
            session.run("CALL db.createEdgeLabel('is_friend','[[\"person\",\"person\"]]')");
            session.run("create (n1:person {name:'jack',id:1}), (n2:person {name:'lucy',id:2})");
            session.run("match (n1:person {id:1}), (n2:person {id:2}) create (n1)-[r:is_friend]->(n2)");
            Result res = session.run("match (n)-[r]->(m) return n,r,m");
            List<Record> records =  res.list();
            for (Record record : records) {
                Node n = record.get("n").asNode();
                System.out.println(n.asMap());
                Relationship r = record.get("r").asRelationship();
                System.out.println(r.asMap());
                Node m = record.get("m").asNode();
                System.out.println(m.asMap());
            }
        } finally {
            // 关闭驱动程序
            driver.close();
        }

    }
}
