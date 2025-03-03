package fr.jixter.badgeuse.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BadgeService {

  private final ReactiveBadgeRepository badgeRepository;
  private final ReactiveEmployeeRepository employeeRepository;

  public BadgeService(ReactiveBadgeRepository badgeRepository, ReactiveEmployeeRepository employeeRepository) {
    this.badgeRepository = badgeRepository;
    this.employeeRepository = employeeRepository;
  }

  public Mono<BadgeRecord> addBadgeRecord(String employeeId, BadgeDto badgeDto) {
    return employeeRepository.findById(employeeId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Employee not found with id " + employeeId)))
        .flatMap(employee -> {
          BadgeRecord record = new BadgeRecord();
          record.setEmployeeId(employee.getId());
          record.setTimestamp(badgeDto.getTimestamp());
          record.setType(badgeDto.getType()); // par exemple "IN" ou "OUT"
          return badgeRepository.save(record);
        });
  }

  public Mono<TimeReport> calculateDailyTime(String employeeId, String date) {
    return badgeRepository.findByEmployeeIdAndDate(employeeId, date)
        .collectList()
        .map(records -> {
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
    return badgeRepository.findByEmployeeIdAndMonth(employeeId, month)
        .collectList()
        .map(records -> {
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
    // Implémentation simplifiée : dans un vrai cas, calculez le nombre de jours ouvrés
    return 20;
  }
}

