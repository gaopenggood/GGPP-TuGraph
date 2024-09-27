package com.ggpp.tugraph.domain;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@Data
public class Movie {
    @Id
    private Long id;

    private String title;
    public Movie(String title) {
        this.title = title;
    }
}
