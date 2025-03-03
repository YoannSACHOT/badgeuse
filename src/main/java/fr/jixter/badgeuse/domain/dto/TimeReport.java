package fr.jixter.badgeuse.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TimeReport {

  private String employeeId;
  // Pour un rapport journalier, on utilisera le champ 'date' (format "yyyy-MM-dd")
  private String date;
  // Pour un rapport mensuel, le champ 'month' pourra être utilisé (format "yyyy-MM")
  private String month;
  private long totalMinutes;
  private boolean sufficient;
}
