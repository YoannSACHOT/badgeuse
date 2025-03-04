package fr.jixter.badgeuse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.jixter.badgeuse.domain.BadgeRecord;
import fr.jixter.badgeuse.domain.BadgeType;
import fr.jixter.badgeuse.domain.Employee;
import fr.jixter.badgeuse.domain.dto.BadgeDto;
import fr.jixter.badgeuse.exception.ResourceNotFoundException;
import fr.jixter.badgeuse.repository.ReactiveEmployeeRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureDataMongo
@ActiveProfiles("test")
class BadgeServiceTests {

  @Autowired private ReactiveEmployeeRepository employeeRepository;

  @Autowired private BadgeService badgeService;

  @Test
  void testAddBadgeRecord_Success() {
    String employeeId = "1";
    Employee employee =
        Employee.builder().id(employeeId).name("John Doe").email("john@example.com").build();

    employeeRepository.save(employee).block();

    LocalDateTime now = LocalDateTime.now();
    BadgeDto badgeDto = BadgeDto.builder().timestamp(now).type(BadgeType.IN).build();

    BadgeRecord expectedBadgeRecord =
        BadgeRecord.builder().employeeId(employeeId).timestamp(now).type(BadgeType.IN).build();

    Mono<BadgeRecord> result = badgeService.addBadgeRecord(employeeId, badgeDto);

    StepVerifier.create(result)
        .assertNext(
            badgeRecord -> {
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

    Mono<BadgeRecord> result = badgeService.addBadgeRecord(employeeId, badgeDto);

    StepVerifier.create(result)
        .expectErrorMatches(
            throwable ->
                throwable instanceof ResourceNotFoundException
                    && throwable.getMessage().equals("Employee not found with id " + employeeId))
        .verify();
  }
}
