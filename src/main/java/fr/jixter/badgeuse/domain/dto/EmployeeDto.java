package fr.jixter.badgeuse.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmployeeDto {

  @NotBlank(message = "Le nom est requis")
  private String name;

  @NotBlank(message = "L'email est requis")
  @Email(message = "L'email doit être valide")
  private String email;
}
