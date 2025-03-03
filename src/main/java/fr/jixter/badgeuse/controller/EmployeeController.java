package fr.jixter.badgeuse.controller;

import fr.jixter.badgeuse.domain.Employee;
import fr.jixter.badgeuse.domain.dto.EmployeeDto;
import fr.jixter.badgeuse.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

  private final EmployeeService employeeService;

  public EmployeeController(EmployeeService employeeService) {
    this.employeeService = employeeService;
  }

  @PostMapping
  public Mono<ResponseEntity<Employee>> createEmployee(@Valid @RequestBody EmployeeDto employeeDto) {
    return employeeService.createEmployee(employeeDto)
        .map(employee -> ResponseEntity.status(HttpStatus.CREATED).body(employee));
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<Employee>> getEmployee(@PathVariable String id) {
    return employeeService.getEmployeeById(id)
        .map(ResponseEntity::ok);
  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<Employee>> updateEmployee(@PathVariable String id,
      @Valid @RequestBody EmployeeDto employeeDto) {
    return employeeService.updateEmployee(id, employeeDto)
        .map(ResponseEntity::ok);
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Void>> deleteEmployee(@PathVariable String id) {
    return employeeService.deleteEmployee(id)
        .thenReturn(ResponseEntity.noContent().build());
  }

  @GetMapping
  public Flux<Employee> getAllEmployees() {
    return employeeService.getAllEmployees();
  }
}
