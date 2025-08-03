package com.emara.task.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emara.task.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    public Employee findByUserId(Integer userId);
}
