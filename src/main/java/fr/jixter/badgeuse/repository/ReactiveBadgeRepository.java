package fr.jixter.badgeuse.repository;

import fr.jixter.badgeuse.domain.BadgeRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveBadgeRepository extends ReactiveMongoRepository<BadgeRecord, String> {

  // Méthode dérivée pour récupérer les enregistrements entre deux timestamps pour un employé donné
  Flux<BadgeRecord> findByEmployeeIdAndTimestampBetween(
      String employeeId, LocalDateTime start, LocalDateTime end);

  // Méthode par défaut pour filtrer par jour (format "yyyy-MM-dd")
  default Flux<BadgeRecord> findByEmployeeIdAndDate(String employeeId, String date) {
    LocalDate localDate = LocalDate.parse(date); // ex: "2025-03-03"
    LocalDateTime startOfDay = localDate.atStartOfDay();
    LocalDateTime endOfDay = localDate.plusDays(1).atStartOfDay();
    return findByEmployeeIdAndTimestampBetween(employeeId, startOfDay, endOfDay);
  }

  // Méthode par défaut pour filtrer par mois (format "yyyy-MM")
  default Flux<BadgeRecord> findByEmployeeIdAndMonth(String employeeId, String month) {
    // On considère le premier jour du mois et on calcule le début et la fin du mois
    LocalDate localDate = LocalDate.parse(month + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    LocalDateTime startOfMonth = localDate.atStartOfDay();
    LocalDateTime endOfMonth = localDate.plusMonths(1).atStartOfDay();
    return findByEmployeeIdAndTimestampBetween(employeeId, startOfMonth, endOfMonth);
  }

  Mono<BadgeRecord> findFirstByEmployeeIdAndTimestampLessThanEqualOrderByTimestampDesc(String employeeId, LocalDateTime timestamp);

}
