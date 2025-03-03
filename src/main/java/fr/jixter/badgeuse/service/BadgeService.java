package fr.jixter.badgeuse.service;

import fr.jixter.badgeuse.domain.BadgeRecord;
import fr.jixter.badgeuse.domain.dto.BadgeDto;
import fr.jixter.badgeuse.domain.dto.TimeReport;
import fr.jixter.badgeuse.exception.ResourceNotFoundException;
import fr.jixter.badgeuse.repository.ReactiveBadgeRepository;
import fr.jixter.badgeuse.repository.ReactiveEmployeeRepository;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class BadgeService {

  private final ReactiveBadgeRepository badgeRepository;
  private final ReactiveEmployeeRepository employeeRepository;

  public Mono<BadgeRecord> addBadgeRecord(String employeeId, BadgeDto badgeDto) {
    return employeeRepository
        .findById(employeeId)
        .switchIfEmpty(
            Mono.error(new ResourceNotFoundException("Employee not found with id " + employeeId)))
        .flatMap(
            employee -> {
              BadgeRecord badgeRecord = new BadgeRecord();
              badgeRecord.setEmployeeId(employee.getId());
              badgeRecord.setTimestamp(badgeDto.getTimestamp());
              badgeRecord.setType(badgeDto.getType()); // par exemple "IN" ou "OUT"
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
              TimeReport report = new TimeReport();
              report.setEmployeeId(employeeId);
              report.setDate(date);
              report.setTotalMinutes(totalMinutes);
              report.setSufficient(sufficient);
              return report;
            });
  }

  public Mono<TimeReport> calculateMonthlyTime(String employeeId, String month) {
    return badgeRepository
        .findByEmployeeIdAndMonth(employeeId, month)
        .collectList()
        .map(
            records -> {
              long totalMinutes = computeTotalMinutes(records);
              // Calcul du nombre de jours ouvrés (implémentation simplifiée)
              int workingDays = calculateWorkingDaysInMonth(month);
              long expectedMinutes = workingDays * 420;
              boolean sufficient = totalMinutes >= expectedMinutes;
              TimeReport report = new TimeReport();
              report.setEmployeeId(employeeId);
              report.setMonth(month);
              report.setTotalMinutes(totalMinutes);
              report.setSufficient(sufficient);
              return report;
            });
  }

  private long computeTotalMinutes(List<BadgeRecord> records) {
    // Tri des enregistrements par timestamp
    records.sort(Comparator.comparing(BadgeRecord::getTimestamp));
    long total = 0;
    for (int i = 0; i < records.size() - 1; i++) {
      BadgeRecord current = records.get(i);
      BadgeRecord next = records.get(i + 1);
      if ("IN".equals(current.getType()) && "OUT".equals(next.getType())) {
        total += Duration.between(current.getTimestamp(), next.getTimestamp()).toMinutes();
        i++; // Passer l'enregistrement associé
      }
    }
    return total;
  }

  private int calculateWorkingDaysInMonth(String month) {
    // TODO: dans un vrai cas, calculez le nombre de jours ouvrés
    return 20;
  }
}
