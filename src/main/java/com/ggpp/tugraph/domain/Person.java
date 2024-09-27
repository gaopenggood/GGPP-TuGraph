package com.ggpp.tugraph.domain;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node
@Data
public class Person {
    @Id
    private Long id;
    private String name;

    // 为了方便，可以添加一个构造方法
    public Person(String name) {
        this.name = name;
    }
    @Relationship(type = "ACTED_IN")
    private Set<Movie> movies = new HashSet<>();

    // 添加出演电影的方法
    public void addMovie(Movie movie) {
        this.movies.add(movie);
    }
}
