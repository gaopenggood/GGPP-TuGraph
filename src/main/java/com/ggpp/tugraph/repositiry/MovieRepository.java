package com.ggpp.tugraph.repositiry;

import com.ggpp.tugraph.domain.Movie;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends Neo4jRepository<Movie, Long> {
}
