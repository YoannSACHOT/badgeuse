package fr.jixter.badgeuse.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@TestConfiguration
public class MongoTestConfig {

  @Bean
  public ReactiveMongoTemplate reactiveMongoTemplate() {
    return mock(ReactiveMongoTemplate.class);
  }
}
