package fr.jixter.badgeuse.service;

import fr.jixter.badgeuse.domain.BadgeRecord;
import fr.jixter.badgeuse.domain.BadgeType;
import fr.jixter.badgeuse.domain.dto.BadgeDto;
import fr.jixter.badgeuse.domain.dto.TimeReport;
import fr.jixter.badgeuse.exception.ResourceNotFoundException;
import fr.jixter.badgeuse.repository.ReactiveBadgeRepository;
import fr.jixter.badgeuse.repository.ReactiveEmployeeRepository;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class BadgeService {

  private static final Logger logger = LoggerFactory.getLogger(BadgeService.class);

  private final ReactiveBadgeRepository badgeRepository;
  private final ReactiveEmployeeRepository employeeRepository;

  public Mono<BadgeRecord> addBadgeRecord(String employeeId, BadgeDto badgeDto) {
    return employeeRepository
        .findById(employeeId)
        .switchIfEmpty(
            Mono.error(new ResourceNotFoundException("Employee not found with id " + employeeId)))
        .flatMap(
            employee -> {
              BadgeRecord badgeRecord =
                  BadgeRecord.builder()
                      .employeeId(employee.getId())
                      .timestamp(badgeDto.getTimestamp())
                      .type(badgeDto.getType())
                      .build();
              return badgeRepository.save(badgeRecord);
            });
  }

  public Mono<TimeReport> calculateDailyTime(String employeeId, String date) {
    return badgeRepository
        .findByEmployeeIdAndDate(employeeId, date)
        .collectList()
        .map(
            records -> {
              long totalMinutes = computeTotalMinutes(records);
              // 7h = 420 minutes par jour
              boolean sufficient = totalMinutes >= 420;
              return TimeReport.builder()
                  .employeeId(employeeId)
                  .date(date)
                  .totalMinutes(totalMinutes)
                  .sufficient(sufficient)
                  .build();
            });
  }

  public Mono<TimeReport> calculateMonthlyTime(String employeeId, String month) {
    return badgeRepository
        .findByEmployeeIdAndMonth(employeeId, month)
        .collectList()
        .map(
            records -> {
              long totalMinutes = computeTotalMinutes(records);
              int workingDays = calculateWorkingDaysInMonth(month);
              long expectedMinutes = workingDays * 420L;
              boolean sufficient = totalMinutes >= expectedMinutes;
              return TimeReport.builder()
                  .employeeId(employeeId)
                  .month(month)
                  .totalMinutes(totalMinutes)
                  .sufficient(sufficient)
                  .build();
            });
  }

  private long computeTotalMinutes(List<BadgeRecord> records) {
    records.sort(Comparator.comparing(BadgeRecord::getTimestamp));
    long total = 0;
    final int size = records.size();
    boolean skipNext = false;

    // On itère jusqu'à l'avant-dernier élément pour traiter les éventuels appariements
    for (int i = 0; i < size - 1; i++) {
      if (skipNext) {
        // On réinitialise l'indicateur et laisse la boucle incrémenter i normalement
        skipNext = false;
        continue;
      }

      BadgeRecord current = records.get(i);
      BadgeRecord next = records.get(i + 1);

      if (BadgeType.IN.equals(current.getType()) && BadgeType.OUT.equals(next.getType())) {
        total += Duration.between(current.getTimestamp(), next.getTimestamp()).toMinutes();
        // On indique que l'élément suivant est déjà traité
        skipNext = true;
      } else {
        logger.warn(
            "Enregistrement non apparié détecté pour l'employé {}: {}",
            current.getEmployeeId(),
            current);
      }
    }

    // Si le dernier enregistrement n'a pas été traité (liste de taille impaire ou dernier
    // enregistrement non apparié)
    if (!skipNext && size > 0) {
      BadgeRecord last = records.get(size - 1);
      logger.warn(
          "Enregistrement non apparié détecté pour l'employé {}: {}", last.getEmployeeId(), last);
    }

    return total;
  }

  private int calculateWorkingDaysInMonth(String month) {
    // Calcul dynamique des jours ouvrés en excluant samedis et dimanches
    YearMonth ym = YearMonth.parse(month);
    int workingDays = 0;
    for (int day = 1; day <= ym.lengthOfMonth(); day++) {
      LocalDate date = ym.atDay(day);
      if (!(date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)) {
        workingDays++;
      }
    }
    return workingDays;
  }
}
