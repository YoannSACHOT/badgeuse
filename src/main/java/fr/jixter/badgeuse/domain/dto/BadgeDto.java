package fr.jixter.badgeuse.domain.dto;

import fr.jixter.badgeuse.domain.BadgeType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BadgeDto {

  @NotNull(message = "Le timestamp est requis")
  private LocalDateTime timestamp;

  @NotNull(message = "Le type (IN/OUT) est requis")
  private BadgeType type;
}
