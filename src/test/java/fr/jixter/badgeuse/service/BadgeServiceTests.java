package fr.jixter.badgeuse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.jixter.badgeuse.domain.BadgeRecord;
import fr.jixter.badgeuse.domain.BadgeType;
import fr.jixter.badgeuse.domain.Employee;
import fr.jixter.badgeuse.domain.dto.BadgeDto;
import fr.jixter.badgeuse.exception.ResourceNotFoundException;
import fr.jixter.badgeuse.repository.ReactiveBadgeRepository;
import fr.jixter.badgeuse.repository.ReactiveEmployeeRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(classes = {BadgeService.class, BadgeServiceTests.MockConfig.class})
@ActiveProfiles("test")
class BadgeServiceTests {

  @TestConfiguration
  static class MockConfig {
    @Bean
    public ReactiveBadgeRepository reactiveBadgeRepository() {
      return mock(ReactiveBadgeRepository.class);
    }

    @Bean
    public ReactiveEmployeeRepository reactiveEmployeeRepository() {
      return mock(ReactiveEmployeeRepository.class);
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() {
      return mock(ReactiveMongoTemplate.class);
    }
  }

  @Autowired private ReactiveBadgeRepository badgeRepository;

  @Autowired private ReactiveEmployeeRepository employeeRepository;

  @Autowired private BadgeService badgeService;

  @Test
  void testAddBadgeRecord_Success() {
    String employeeId = "1";
    Employee employee =
        Employee.builder().id(employeeId).name("John Doe").email("john@example.com").build();

    LocalDateTime now = LocalDateTime.now();
    BadgeDto badgeDto = BadgeDto.builder().timestamp(now).type(BadgeType.IN).build();

    BadgeRecord expectedBadgeRecord =
        BadgeRecord.builder()
            .id("123")
            .employeeId(employeeId)
            .timestamp(now)
            .type(BadgeType.IN)
            .build();

    when(employeeRepository.findById(employeeId)).thenReturn(Mono.just(employee));
    when(badgeRepository.save(any(BadgeRecord.class))).thenReturn(Mono.just(expectedBadgeRecord));

    Mono<BadgeRecord> result = badgeService.addBadgeRecord(employeeId, badgeDto);

    StepVerifier.create(result)
        .assertNext(
            badgeRecord -> {
              assertEquals(expectedBadgeRecord.getId(), badgeRecord.getId());
              assertEquals(expectedBadgeRecord.getEmployeeId(), badgeRecord.getEmployeeId());
              assertEquals(expectedBadgeRecord.getTimestamp(), badgeRecord.getTimestamp());
              assertEquals(expectedBadgeRecord.getType(), badgeRecord.getType());
            })
        .verifyComplete();
  }

  @Test
  void testAddBadgeRecord_EmployeeNotFound() {
    String employeeId = "nonexistent";
    BadgeDto badgeDto =
        BadgeDto.builder().timestamp(LocalDateTime.now()).type(BadgeType.IN).build();

    when(employeeRepository.findById(employeeId)).thenReturn(Mono.empty());

    Mono<BadgeRecord> result = badgeService.addBadgeRecord(employeeId, badgeDto);

    StepVerifier.create(result)
        .expectErrorMatches(
            throwable ->
                throwable instanceof ResourceNotFoundException
                    && throwable.getMessage().equals("Employee not found with id " + employeeId))
        .verify();
  }
}
