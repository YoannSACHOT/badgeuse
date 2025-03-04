package fr.jixter.badgeuse.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.jixter.badgeuse.domain.BadgeRecord;
import fr.jixter.badgeuse.domain.BadgeType;
import fr.jixter.badgeuse.domain.Employee;
import fr.jixter.badgeuse.domain.dto.BadgeDto;
import fr.jixter.badgeuse.domain.dto.TimeReport;
import fr.jixter.badgeuse.exception.ResourceNotFoundException;
import fr.jixter.badgeuse.repository.ReactiveBadgeRepository;
import fr.jixter.badgeuse.repository.ReactiveEmployeeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTests {

  @Mock
  private ReactiveEmployeeRepository employeeRepository;

  @Mock
  private ReactiveBadgeRepository badgeRepository;

  @InjectMocks
  private BadgeService badgeService;

  @Test
  void testAddBadgeRecord_Success() {
    String employeeId = "1";
    Employee employee = Employee.builder()
        .id(employeeId)
        .name("John Doe")
        .email("john@example.com")
        .build();

    LocalDateTime now = LocalDateTime.now();
    BadgeDto badgeDto = BadgeDto.builder()
        .timestamp(now)
        .type(BadgeType.IN)
        .build();

    BadgeRecord badgeRecord = BadgeRecord.builder()
        .id("badgeId")
        .employeeId(employeeId)
        .timestamp(now)
        .type(BadgeType.IN)
        .build();

    // Simulation du repository : l'employé est trouvé et le badge est sauvegardé
    when(employeeRepository.findById(employeeId)).thenReturn(Mono.just(employee));
    when(badgeRepository.save(any(BadgeRecord.class))).thenReturn(Mono.just(badgeRecord));

    Mono<BadgeRecord> result = badgeService.addBadgeRecord(employeeId, badgeDto);

    StepVerifier.create(result)
        .assertNext(badgeRecord1 -> {
          assert(badgeRecord1.getEmployeeId().equals(employeeId));
          assert(badgeRecord1.getTimestamp().equals(now));
          assert(badgeRecord1.getType() == BadgeType.IN);
        })
        .verifyComplete();

    verify(employeeRepository).findById(employeeId);
    verify(badgeRepository).save(any(BadgeRecord.class));
  }

  @Test
  void testAddBadgeRecord_EmployeeNotFound() {
    String employeeId = "nonexistent";
    BadgeDto badgeDto = BadgeDto.builder()
        .timestamp(LocalDateTime.now())
        .type(BadgeType.IN)
        .build();

    // Simulation : aucun employé trouvé
    when(employeeRepository.findById(employeeId)).thenReturn(Mono.empty());

    Mono<BadgeRecord> result = badgeService.addBadgeRecord(employeeId, badgeDto);

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof ResourceNotFoundException &&
                throwable.getMessage().equals("Employee not found with id " + employeeId))
        .verify();
  }

  @Test
  void testCalculateDailyTime() {
    String employeeId = "2";
    String date = "2025-03-03";
    LocalDate localDate = LocalDate.parse(date);
    LocalDateTime inTime = localDate.atTime(9, 0);
    LocalDateTime outTime = localDate.atTime(17, 0);

    BadgeRecord inRecord = BadgeRecord.builder()
        .employeeId(employeeId)
        .timestamp(inTime)
        .type(BadgeType.IN)
        .build();
    BadgeRecord outRecord = BadgeRecord.builder()
        .employeeId(employeeId)
        .timestamp(outTime)
        .type(BadgeType.OUT)
        .build();

    List<BadgeRecord> records = Arrays.asList(inRecord, outRecord);
    when(badgeRepository.findByEmployeeIdAndDate(employeeId, date)).thenReturn(Flux.fromIterable(records));

    Mono<TimeReport> reportMono = badgeService.calculateDailyTime(employeeId, date);

    StepVerifier.create(reportMono)
        .assertNext(report -> {
          assert(report.getEmployeeId().equals(employeeId));
          assert(report.getDate().equals(date));
          // Durée totale : 8 heures = 480 minutes
          assert(report.getTotalMinutes() == 480);
          // Suffisance : 480 minutes >= 420 minutes attendues
          assert(report.isSufficient());
        })
        .verifyComplete();
  }

  @Test
  void testCalculateMonthlyTime() {
    String employeeId = "3";
    String month = "2025-03";
    // Simulation de deux jours avec 8h (480 min) et 7h (420 min)
    LocalDate date1 = LocalDate.parse("2025-03-03");
    LocalDate date2 = LocalDate.parse("2025-03-04");
    LocalDateTime in1 = date1.atTime(9, 0);
    LocalDateTime out1 = in1.plusHours(8);
    LocalDateTime in2 = date2.atTime(9, 0);
    LocalDateTime out2 = in2.plusHours(7);

    BadgeRecord record1In = BadgeRecord.builder()
        .employeeId(employeeId)
        .timestamp(in1)
        .type(BadgeType.IN)
        .build();
    BadgeRecord record1Out = BadgeRecord.builder()
        .employeeId(employeeId)
        .timestamp(out1)
        .type(BadgeType.OUT)
        .build();
    BadgeRecord record2In = BadgeRecord.builder()
        .employeeId(employeeId)
        .timestamp(in2)
        .type(BadgeType.IN)
        .build();
    BadgeRecord record2Out = BadgeRecord.builder()
        .employeeId(employeeId)
        .timestamp(out2)
        .type(BadgeType.OUT)
        .build();

    List<BadgeRecord> records = Arrays.asList(record1In, record1Out, record2In, record2Out);
    when(badgeRepository.findByEmployeeIdAndMonth(employeeId, month)).thenReturn(Flux.fromIterable(records));

    Mono<TimeReport> reportMono = badgeService.calculateMonthlyTime(employeeId, month);

    StepVerifier.create(reportMono)
        .assertNext(report -> {
          // Total attendu : 480 + 420 = 900 minutes
          assert(report.getEmployeeId().equals(employeeId));
          assert(report.getMonth().equals(month));
          assert(report.getTotalMinutes() == 900);
        })
        .verifyComplete();
  }
}
