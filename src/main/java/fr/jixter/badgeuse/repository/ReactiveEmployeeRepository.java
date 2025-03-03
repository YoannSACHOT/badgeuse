package fr.jixter.badgeuse.repository;

import fr.jixter.badgeuse.domain.Employee;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ReactiveEmployeeRepository extends ReactiveMongoRepository<Employee, String> {}
