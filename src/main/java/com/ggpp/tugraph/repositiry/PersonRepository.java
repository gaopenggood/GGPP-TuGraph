package com.ggpp.tugraph.repositiry;

import com.ggpp.tugraph.domain.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends Neo4jRepository<Person, Long> {
    Optional<Person> findByName(String name);
}
