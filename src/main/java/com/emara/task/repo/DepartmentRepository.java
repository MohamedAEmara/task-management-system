package com.emara.task.repo;

import com.emara.task.model.Department;
import com.emara.task.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

}
