package fr.jixter.badgeuse.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "employees")
public class Employee {

  @Id private String id;

  @NotBlank(message = "Le nom est requis")
  private String name;

  @NotBlank(message = "L'email est requis")
  @Email(message = "L'email doit être valide")
  private String email;
}
