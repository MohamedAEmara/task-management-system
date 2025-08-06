package com.emara.task.service;

import com.emara.task.dto.AddEmployeeToDepartmentDto;
import com.emara.task.dto.CreateDepartmentDto;
import com.emara.task.model.Department;
import com.emara.task.model.Employee;
import com.emara.task.model.Manager;
import com.emara.task.model.User;
import com.emara.task.repo.DepartmentRepository;
import com.emara.task.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private ManagerService managerService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    public ResponseEntity<?> createDepartment(CreateDepartmentDto request) {
        Department department = new Department();
        department.setName(request.getName());
        Manager manager = managerService.findManagerByUserId(request.getUserId());
        department.setManager(manager);
        departmentRepository.save(department);
        return ResponseEntity.ok(department);
    }

    public ResponseEntity<?> addEmployeeToDepartment(AddEmployeeToDepartmentDto dto) {
        try {
            if (dto.getDepartmentId() == null || dto.getUserId() == null) {
                throw new BadRequestException("Missing departmentId or userId fields in request body");
            }
            Optional<Department> departmentOpt = departmentRepository.findById(dto.getDepartmentId());
            Employee employee = employeeService.findByUserId(dto.getUserId());
            if (departmentOpt.isEmpty()) {
                throw new EntityNotFoundException("Department not found!");
            }
            if (employee == null) {
                throw new UsernameNotFoundException("User not found!");
            }
            Department department = departmentOpt.get();
            UserDetails userDetails = jwtUtil.getUser();

            // Verify user is the manager of the department
            User user = userService.findByUsername(userDetails.getUsername());
            Manager manager = managerService.findManagerByUserId(user.getId()); // Assuming username is user_id
            if (manager == null || !department.getManager().getId().equals(manager.getId())) {
                System.out.println("USER IS NOT THE MANAGER OF THIS DEPARTMENT");
                throw new BadRequestException("User is not the manager of this department!");
            }

            List<Employee> employees = department.getEmployees();

            if (employees == null) {
                employees = new ArrayList<>();
                department.setEmployees(employees);
            }
            if (!employees.contains(employee) && employee.getDepartment() ==     null) {
                employees.add(employee); // Add employee to department's list
                employee.setDepartment(department); // Set department in employee
                departmentRepository.save(department); // Save department
                employeeService.save(employee); // Save employee to update department_id
            } else if (employee.getDepartment() != null) {
                throw new BadRequestException("This employee is already assigned to a department!");
            }
            return ResponseEntity.ok(department);
        } catch (BadRequestException | EntityNotFoundException | UsernameNotFoundException ex) {
            return ResponseEntity.badRequest().body("Error adding employee to department: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.status(500).body("Error adding employee to department: " + ex.getMessage());
        }
    }


    public ResponseEntity<?> removeEmployeeFromDepartment(AddEmployeeToDepartmentDto dto) {
        try {
            if (dto.getDepartmentId() == null || dto.getUserId() == null) {
                throw new BadRequestException("Missing departmentId or userId fields in request body");
            }
            Optional<Department> departmentOpt = departmentRepository.findById(dto.getDepartmentId());
            Employee employee = employeeService.findByUserId(dto.getUserId());
            if (departmentOpt.isEmpty()) {
                throw new EntityNotFoundException("Department not found!");
            }
            if (employee == null) {
                throw new UsernameNotFoundException("User not found!");
            }
            Department department = departmentOpt.get();

            UserDetails userDetails = jwtUtil.getUser();

            // Verify user is the manager of the department
            User user = userService.findByUsername(userDetails.getUsername());
            Manager manager = managerService.findManagerByUserId(user.getId()); // Assuming username is user_id
            if (manager == null || !department.getManager().getId().equals(manager.getId())) {
                System.out.println("USER IS NOT THE MANAGER OF THIS DEPARTMENT");
                throw new BadRequestException("User is not the manager of this department!");
            }
            List<Employee> employees = department.getEmployees();

            if(!employees.contains(employee)) {
                throw new Exception("Employee not in this department");
            }
            employees.remove(employee);
            employee.setDepartment(null); // Set department in employee
            departmentRepository.save(department);
            employeeService.save(employee);
            return ResponseEntity.ok("Employee removed from department successfully!");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.status(500).body("Error adding employee to department: " + ex.getMessage());
        }
    }

}
