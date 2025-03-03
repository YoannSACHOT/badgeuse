package fr.jixter.badgeuse.controller;

import fr.jixter.badgeuse.domain.BadgeRecord;
import fr.jixter.badgeuse.domain.dto.BadgeDto;
import fr.jixter.badgeuse.domain.dto.TimeReport;
import fr.jixter.badgeuse.service.BadgeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {

  private final BadgeService badgeService;

  public BadgeController(BadgeService badgeService) {
    this.badgeService = badgeService;
  }

  @PostMapping("/employee/{employeeId}")
  public Mono<ResponseEntity<BadgeRecord>> addBadgeRecord(
      @PathVariable String employeeId, @Valid @RequestBody BadgeDto badgeDto) {
    return badgeService
        .addBadgeRecord(employeeId, badgeDto)
        .map(badgeRecord -> ResponseEntity.status(HttpStatus.CREATED).body(badgeRecord));
  }

  @GetMapping("/employee/{employeeId}/day/{date}")
  public Mono<ResponseEntity<TimeReport>> getDailyTimeReport(
      @PathVariable String employeeId, @PathVariable String date) {
    return badgeService.calculateDailyTime(employeeId, date).map(ResponseEntity::ok);
  }

  @GetMapping("/employee/{employeeId}/month/{month}")
  public Mono<ResponseEntity<TimeReport>> getMonthlyTimeReport(
      @PathVariable String employeeId, @PathVariable String month) {
    return badgeService.calculateMonthlyTime(employeeId, month).map(ResponseEntity::ok);
  }
}
