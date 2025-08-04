package com.emara.task.service;

import com.emara.task.dto.CreateDepartmentDto;
import com.emara.task.model.Department;
import com.emara.task.model.Manager;
import com.emara.task.repo.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private ManagerService managerService;

    public ResponseEntity<?> createDepartment(CreateDepartmentDto request) {
        Department department = new Department();
        department.setName(request.getName());
        Manager manager = managerService.findManagerByUserId(request.getUserId());
        department.setManager(manager);
        departmentRepository.save(department);
        return ResponseEntity.ok(department);
    }
}
