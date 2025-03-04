package fr.jixter.badgeuse.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.jixter.badgeuse.domain.Employee;
import fr.jixter.badgeuse.domain.dto.EmployeeDto;
import fr.jixter.badgeuse.exception.ResourceNotFoundException;
import fr.jixter.badgeuse.repository.ReactiveEmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTests {

  @Mock
  private ReactiveEmployeeRepository employeeRepository;

  @InjectMocks
  private EmployeeService employeeService;

  @Test
  void testCreateEmployee() {
    EmployeeDto employeeDto = EmployeeDto.builder()
        .name("John Doe")
        .email("john@example.com")
        .build();
    Employee savedEmployee = Employee.builder()
        .id("1")
        .name("John Doe")
        .email("john@example.com")
        .build();

    when(employeeRepository.save(any(Employee.class)))
        .thenReturn(Mono.just(savedEmployee));

    Mono<Employee> result = employeeService.createEmployee(employeeDto);

    StepVerifier.create(result)
        .assertNext(employee -> {
          assert employee.getId().equals("1");
          assert employee.getName().equals("John Doe");
          assert employee.getEmail().equals("john@example.com");
        })
        .verifyComplete();

    verify(employeeRepository).save(any(Employee.class));
  }

  @Test
  void testGetEmployeeById_Found() {
    Employee employee = Employee.builder()
        .id("1")
        .name("John Doe")
        .email("john@example.com")
        .build();

    when(employeeRepository.findById("1")).thenReturn(Mono.just(employee));

    Mono<Employee> result = employeeService.getEmployeeById("1");

    StepVerifier.create(result)
        .assertNext(e -> {
          assert e.getId().equals("1");
          assert e.getName().equals("John Doe");
          assert e.getEmail().equals("john@example.com");
        })
        .verifyComplete();
  }

  @Test
  void testGetEmployeeById_NotFound() {
    when(employeeRepository.findById("nonexistent")).thenReturn(Mono.empty());

    Mono<Employee> result = employeeService.getEmployeeById("nonexistent");

    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof ResourceNotFoundException &&
                throwable.getMessage().equals("Employee not found with id nonexistent"))
        .verify();
  }

  @Test
  void testUpdateEmployee() {
    Employee existingEmployee = Employee.builder()
        .id("1")
        .name("John Doe")
        .email("john@example.com")
        .build();
    EmployeeDto updateDto = EmployeeDto.builder()
        .name("Jane Doe")
        .email("jane@example.com")
        .build();
    Employee updatedEmployee = Employee.builder()
        .id("1")
        .name("Jane Doe")
        .email("jane@example.com")
        .build();

    when(employeeRepository.findById("1")).thenReturn(Mono.just(existingEmployee));
    when(employeeRepository.save(any(Employee.class))).thenReturn(Mono.just(updatedEmployee));

    Mono<Employee> result = employeeService.updateEmployee("1", updateDto);

    StepVerifier.create(result)
        .assertNext(employee -> {
          assert employee.getId().equals("1");
          assert employee.getName().equals("Jane Doe");
          assert employee.getEmail().equals("jane@example.com");
        })
        .verifyComplete();
  }

  @Test
  void testDeleteEmployee() {
    Employee existingEmployee = Employee.builder()
        .id("1")
        .name("John Doe")
        .email("john@example.com")
        .build();

    when(employeeRepository.findById("1")).thenReturn(Mono.just(existingEmployee));
    when(employeeRepository.delete(existingEmployee)).thenReturn(Mono.empty());

    Mono<Void> result = employeeService.deleteEmployee("1");

    StepVerifier.create(result)
        .verifyComplete();

    verify(employeeRepository).findById("1");
    verify(employeeRepository).delete(existingEmployee);
  }

  @Test
  void testGetAllEmployees() {
    Employee employee1 = Employee.builder()
        .id("1")
        .name("John Doe")
        .email("john@example.com")
        .build();
    Employee employee2 = Employee.builder()
        .id("2")
        .name("Jane Doe")
        .email("jane@example.com")
        .build();

    when(employeeRepository.findAll()).thenReturn(Flux.just(employee1, employee2));

    Flux<Employee> result = employeeService.getAllEmployees();

    StepVerifier.create(result)
        .expectNext(employee1)
        .expectNext(employee2)
        .verifyComplete();

    verify(employeeRepository).findAll();
  }
}
