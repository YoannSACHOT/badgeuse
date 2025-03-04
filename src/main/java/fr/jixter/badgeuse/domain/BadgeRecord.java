package fr.jixter.badgeuse.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "badge_records")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BadgeRecord {

  @Id
  private String id;

  @NotBlank(message = "L'ID de l'employé est requis")
  private String employeeId;

  @NotNull(message = "Le timestamp est requis")
  private LocalDateTime timestamp;

  @NotNull(message = "Le type (IN/OUT) est requis")
  private BadgeType type;
}
