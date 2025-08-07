package com.emara.task.repo;

import com.emara.task.model.Department;
import com.emara.task.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    @Query("select dept from Department dept")
    public List<Department> getAll();

    @Query("select dept from Department dept where dept.manager.id = :managerId")
    public List<Department> findByManagerId(Integer managerId);
}
