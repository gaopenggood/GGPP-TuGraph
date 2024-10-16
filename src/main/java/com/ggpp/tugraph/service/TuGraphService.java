package com.ggpp.tugraph.service;

import com.ggpp.tugraph.domain.Movie;
import com.ggpp.tugraph.domain.Person;
import com.ggpp.tugraph.repositiry.MovieRepository;
import com.ggpp.tugraph.repositiry.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TuGraphService {

    private final Neo4jClient neo4jClient;

    // 通过构造函数注入Neo4jClient
    @Autowired
    public TuGraphService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MovieRepository movieRepository;

    public void savePerson(String name) {
        Person person = new Person(name);
        personRepository.save(person);
    }

    public void createData() {
        // Create new Person and Movie nodes
        Person alice = new Person("Alice");
        Movie inception = new Movie("Inception");

        // Save the nodes (this will also assign them an ID if they don't have one yet)
        personRepository.save(alice);
        movieRepository.save(inception);

        // Create the relationship between the nodes
        alice.addMovie(inception);

        // Save the updated Person node to persist the relationship
        personRepository.save(alice);
    }

    public void findMoviesByPersonName(String name) {
        // In a real application, you would use a custom query method in the PersonRepository
        // For simplicity, here we're just showing how to use the repository to find all people and then filter
        Iterable<Person> people = personRepository.findAll();
        for (Person person : people) {
            if (person.getName().equals(name)) {
                System.out.println("Movies acted in by " + person.getName() + ":");
                for (Movie movie : person.getMovies()) {
                    System.out.println(movie.getTitle());
                }
            }
        }
    }


    public void doText() {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "GGpp1993@"));
        var result = driver.executableQuery("match (p: Person)-[a:ACTED_IN]->(m: Movie) where m.title = 'Sense and Sensibility'  return p,m,a")
                .withConfig(QueryConfig.builder().withDatabase("neo4j").build())
                .execute();

        var records = result.records();
        records.forEach(r -> {
            System.out.println(r);  // or r.get("name").asString()
        });

        var summary = result.summary();
        System.out.printf("The query %s returned %d records in %d ms.%n",
                summary.query(), records.size(),
                summary.resultAvailableAfter(TimeUnit.MILLISECONDS));
    }
}
