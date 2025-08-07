package com.emara.task.controller;

import com.emara.task.dto.AddEmployeeToDepartmentDto;
import com.emara.task.dto.CreateDepartmentDto;
import com.emara.task.model.Department;
import com.emara.task.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/department")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody CreateDepartmentDto request) {
        return departmentService.createDepartment(request);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addEmployeeToDepartment(@RequestBody AddEmployeeToDepartmentDto request) {
        return departmentService.addEmployeeToDepartment(request);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeEmployeeFromDepartment(@RequestBody AddEmployeeToDepartmentDto request) {
        return departmentService.removeEmployeeFromDepartment(request);
    }

    @GetMapping()
    public ResponseEntity<?> getMyDepartments() {
        return departmentService.getAll();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyDepartment() {
        return departmentService.getMyDepartment();
    }
}
