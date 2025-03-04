package fr.jixter.badgeuse.service;

import fr.jixter.badgeuse.domain.Employee;
import fr.jixter.badgeuse.domain.dto.EmployeeDto;
import fr.jixter.badgeuse.exception.ResourceNotFoundException;
import fr.jixter.badgeuse.repository.ReactiveEmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class EmployeeService {

  private final ReactiveEmployeeRepository employeeRepository;

  public Mono<Employee> createEmployee(EmployeeDto employeeDto) {
    Employee employee = new Employee();
    employee.setName(employeeDto.getName());
    employee.setEmail(employeeDto.getEmail());
    return employeeRepository.save(employee);
  }

  public Mono<Employee> getEmployeeById(String id) {
    return employeeRepository
        .findById(id)
        .switchIfEmpty(
            Mono.error(new ResourceNotFoundException("Employee not found with id " + id)));
  }

  public Mono<Employee> updateEmployee(String id, EmployeeDto employeeDto) {
    return getEmployeeById(id)
        .flatMap(
            existingEmployee -> {
              existingEmployee.setName(employeeDto.getName());
              existingEmployee.setEmail(employeeDto.getEmail());
              return employeeRepository.save(existingEmployee);
            });
  }

  public Mono<Void> deleteEmployee(String id) {
    return getEmployeeById(id).flatMap(employeeRepository::delete);
  }

  public Flux<Employee> getAllEmployees() {
    return employeeRepository.findAll();
  }
}
